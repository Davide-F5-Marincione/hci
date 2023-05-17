import os
import asyncio
import data_structures as tds
from typing import Any, List, Tuple, Union
import numpy as np
import random
import matplotlib.pyplot as plt
from dataclasses import dataclass, asdict, field
import time
import jsonpickle
import names
import sys
import logging
import requests

file_handler = logging.FileHandler(filename='sim.log')
stdout_handler = logging.StreamHandler(stream=sys.stdout)
handlers = [file_handler, stdout_handler]

logging.basicConfig(format='%(asctime)s,%(msecs)03d %(levelname)-8s [%(filename)s:%(lineno)d] %(message)s',
    datefmt='%Y-%m-%d:%H:%M:%S',
    level=logging.INFO, handlers=handlers)


PROB_TO_USE_APP = 0.2
PROB_TO_UNBOARD = .3

SIGN_PROB = 0.5

OVERCROWDING_THRESHOLD = 0.8
MIN_OVERCROWD_PROB = .4
MAX_OVERCROWD_PROB = .8

FALLOFF = 0.02

SPEED = 200
CAPACITY = 50

###
#
#   utility functions
#
###
distance = lambda x, y: np.linalg.norm(x - y)

###
#
#   simulation data
#
###


@dataclass
class SimulationEnv:
    stops: List[tds.BusStop]
    routes: List[tds.Route]
    buses: List[tds.Bus]
    passengers: List[tds.Passenger]
    time: float = 0.0

    def save(self, filepath: str, indent: int = 4):
        with open(filepath, "w") as f:
            f.write(jsonpickle.encode(self, indent=indent))


# bus stops

bus_stops = [
    tds.BusStop(2000.0, -1000.0, "A", set()),
    tds.BusStop(1000.5, 4000.0, "B", set()),
    tds.BusStop(-5000, 1000.0, "C", set()),
    tds.BusStop(-2000.0, 1050., "D", set()),
]

# bus routes

routes = [
    tds.Route("A", bus_stops),
]

# time constants

STOP_AT: float = 20000.0
# negative value means run forever
# coroutines still need to yield control to the event loop
# so this is more of a suggestion than a hard stop
# I am NOT going working with synchronization primitives here :)
# (you're welcome to try though).


###
#
#   simulation coroutine
#
###


class Ticker:
    def __init__(self):
        self.last_time = time.time()

    def __call__(self, env: SimulationEnv) -> Any:
        # get time since program start

        time_now = time.time()
        time_delta = time_now - self.last_time

        # update time
        env.time += time_delta

        # update last time
        self.last_time = time_now


async def tick_time(env: SimulationEnv):
    # Everything is "tickless", this is only used for the simplex noise generator
    # and to stop the simulation.

    # change this to change the tick rate of the simulation
    TICK_TIME = 0.1

    logging.info("Simulation starting")

    ticker = Ticker()

    await asyncio.sleep(1) # sleep for 1 second to let the simulation start

    while env.time < STOP_AT:
        await asyncio.sleep(TICK_TIME)

        ticker(env)

    await asyncio.sleep(1)
    logging.info("Simulation complete")


async def simulate_bus(bus: tds.Bus, env: SimulationEnv):
    # create a list of stops to traverse that start with the original position

    starting_idx = bus.curr_pos_idx

    #concat three lists so that the bus goes back and forth correctly
    cycle_path = bus.route[starting_idx:] + bus.route[-2::-1] + bus.route[1:starting_idx]

    if cycle_path[0] == cycle_path[-1]:
        cycle_path = cycle_path[:-1] # i'm tired, boss.

    bus.next_idx = (starting_idx + 1) % len(bus.route)

    logging.info(f"Bus {bus.name} starting simulation")
    logging.info(f"Bus {bus.name} will traverse {cycle_path}")

    def board():
        new_passengers = []
        for _ in range(random.randint(0, bus.capacity - len(bus.on_board))):
            passenger = tds.Passenger(
                name = names.get_first_name(),
                surname = names.get_last_name(),
                uses_our_app = random.random() < PROB_TO_USE_APP,
                bus_he_is_on=bus
            )
            bus.on_board.add(passenger)
            new_passengers.append(passenger)
            logging.info(f"Passenger {passenger.name} {passenger.surname} boarded bus {bus.name}")
        
        env.passengers.extend(new_passengers)

        [  # spawn passenger coroutines, but don't await them (they will run in the background)
            asyncio.create_task(simulate_passenger(passenger, env))
            for passenger in new_passengers
        ]

    def unboard():
        to_remove = set()
        for passenger_out in bus.on_board:
            if random.random() < PROB_TO_UNBOARD:
                passenger_out.bus_he_is_on = None
                passenger_out.arrived = True
                to_remove.add(passenger_out)

                logging.info(f"Passenger {passenger_out.name} {passenger_out.surname} left bus {bus.name}")
        bus.on_board.difference_update(to_remove)


    while env.time < STOP_AT:
        for idx, place in enumerate(cycle_path):

            if env.time >= STOP_AT:
                break

            if isinstance(place, tds.BusStop):
                bus.curr_pos = place
                bus.curr_pos_idx = idx
                bus.next_idx = (idx + 1) % len(cycle_path)

                # Arrives at stop
                place.buses.add(bus)
                logging.info(f"Bus {bus.name} is arriving at {place.name}")

                unboard()

                board()

                logging.info(f"Bus {bus.name} is waiting at {place.name}")
                for _ in range (4): # wait for 1 minute, board 2 times

                    await asyncio.sleep(15)
                    board()

                # By the topology, this is guaranteed to be a bus stop
                # so the one we are checking 2 ahead is also a bus stop

                next_idx = (idx + 2) % len(cycle_path)

                next_place = cycle_path[next_idx]

                # pre-emptively wait for the next bus to go away
                # from the next stop
                # this is hacky but improves the simulation's traffic flow

                while len(next_place.buses) > 0 and any(next_bus.next_idx == bus.curr_pos_idx in
                                                        next_place.buses for next_bus in next_place.buses):

                    # we should divide by the speed of the OTHER bus, not this one
                    # but they are likely to be the same, so it's fine.
                    time_to_wait = tds.distance_between_bus_stops(place, next_place) / bus.speed
                    time_to_wait *= 0.75

                    logging.info((f"Bus {bus.name} is waiting {time_to_wait:.2f}s for a bus to leave {next_place.name} "
                                  f"Current next bus is {next_place.buses}, next hop is {cycle_path[next_idx].name}"))
                    await asyncio.sleep(time_to_wait)
                    board()

                place.buses.remove(bus)

                # prendere passeggeri
                # TODO: 1. settare un timestamp di partenza
                # TODO: 2. setti una flag che abilità la segnalazione

                bus.last_stop = bus.curr_pos
                bus.next_stop = next_place
                bus.departure_time = env.time
                for passenger in bus.on_board:
                    passenger.can_report = True

                await asyncio.sleep(1) # yield control to the event loop

                # leave stop
            elif isinstance(place, tds.RoadConnection):
                # it should get here without awaiting
                bus.curr_pos = place
                bus.curr_pos_idx = idx
                bus.next_idx = (idx + 1) % len(cycle_path)

                distance_left = place.distance
                tenth = distance_left / 10

                logging.info(f"Bus {bus.name} is travelling to {place.name}")

                for passenger in bus.on_board:
                    passenger.can_report = False

                last_dist = 0.0
                last_time = env.time

                while distance_left > 0:
                    await asyncio.sleep(1)

                    curr_time = env.time
                    time_delta = curr_time - last_time

                    trav = bus.speed * time_delta * place.trafficFunc(env.time)
                    last_dist += trav
                    distance_left -= trav

                    if last_dist >= tenth:
                        logging.info(f"Bus {bus.name} is travelling {tenth:.1f}m to {place.name},"
                                    f" {distance_left:.1f}m left, ETA: {(distance_left / bus.speed / .6):.2f}s {place.trafficFunc(env.time):.2f}"
                                    f" traffic factor at {place.name}")
                        last_dist -= tenth
                    
                    last_time = curr_time

                await asyncio.sleep(1)  # yield control to the event loop
        await asyncio.sleep(1)  # yield control to the event loop


async def simulate_passenger(passenger: tds.Passenger, env: SimulationEnv):

    while env.time < STOP_AT:

        # passengers tick every 15 seconds, checking what their state is
        # they get carried by the bus, so they don't need to do anything
        # by themselves, but they might perform some actions when they
        # tick, such as reporting or despawning

        if passenger.bus_he_is_on is not None and passenger.uses_our_app and passenger.can_report:

            bus = passenger.bus_he_is_on
            time_departed = env.time - bus.departure_time

            if len(bus.on_board) >= (OVERCROWDING_THRESHOLD * bus.capacity) and not passenger.reported_overcrowding:
                p = (len(bus.on_board) / bus.capacity - OVERCROWDING_THRESHOLD) / (1 - OVERCROWDING_THRESHOLD) * (MAX_OVERCROWD_PROB - MIN_OVERCROWD_PROB) + MIN_OVERCROWD_PROB
                p = p / (FALLOFF * time_departed + 1)
                if random.random() < p:
                    passenger.reported_overcrowding = True
                    logging.warning(f"Passenger {passenger.name} {passenger.surname} reports overcrowding on bus {bus.name}")
                    requests.put(f"http://localhost:5000/buses/{bus.name}", json={
                        "overcrowded": True
                    })

            if not passenger.reported_boarding:
                p = SIGN_PROB / (FALLOFF * time_departed + 1)
                if random.random() < p:

                    logging.info(f"Passenger {passenger.name} {passenger.surname} reports boarding on bus {bus.name}")

                    passenger.reported_boarding = True

                    requests.put(f"http://localhost:5000/buses/{bus.name}", json={
                        "boardedat": 0.0,
                        "from": bus.last_stop.name,
                        "to": bus.next_stop.name
                    })

        elif passenger.arrived:

            # nice!

            logging.info(f"Passenger {passenger.name} {passenger.surname} leaves the simulation, say bye!")

            env.passengers.remove(passenger)

            break # despawn
        await asyncio.sleep(5+random.random()) # scrambling the wait to avoid huge chunks of work at once

# tells us what's going on in the simulation every 5 minutes
async def env_dumper(env: SimulationEnv):
    while env.time < STOP_AT:

        logging.info((f"ENVDUMP:\n\tCurrent time: {env.time}\n\tTotal passengers: {len(env.passengers)}\n\t"))

        await asyncio.sleep(300)


async def main():
    buses = [
        tds.Bus(
        name = "A-1",
        capacity = CAPACITY,
        speed= SPEED,
        route = routes[0],
        curr_pos_idx = 0),
        tds.Bus(
        name = "B-1",
        capacity = CAPACITY,
        speed= SPEED,
        route = routes[0],
        curr_pos_idx = -1),
    ]

    env = SimulationEnv(bus_stops, routes, buses, [])

    await asyncio.gather(
        tick_time(env),
        *[simulate_bus(bus, env) for bus in buses],
        env_dumper(env)
    )

    #env.save("sim.json")


if __name__ == "__main__":

    logging.info("log separator\n\n\n---Starting simulation---\n\n")

    asyncio.run(main())

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

file_handler = logging.FileHandler(filename='sim.log')
stdout_handler = logging.StreamHandler(stream=sys.stdout)
handlers = [file_handler, stdout_handler]

logging.basicConfig(format='%(asctime)s,%(msecs)03d %(levelname)-8s [%(filename)s:%(lineno)d] %(message)s',
    datefmt='%Y-%m-%d:%H:%M:%S',
    level=logging.INFO, handlers=handlers)


OVERCROWDING_THRESHOLD = 0.8
MAX_PASSENGERS_IN_SIM = 256
PROB_TO_USE_APP = 0.1

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
    stops_to_traverse = bus.route[bus.curr_pos_idx :] + bus.route[: bus.curr_pos_idx]

    logging.info(f"Bus {bus.name} starting simulation")

    def board():
        to_remove = set()

        for passenger_in in bus.curr_pos.waiting:
            # this has scary performance implications...
            if passenger_in.planned_trip.to in bus.route:
                to_remove.add(passenger_in)

                passenger_in.on_bus = True
                bus.on_board.add(passenger_in)
                passenger_in.bus_he_is_on = bus

                logging.info(f"Passenger {passenger_in.name} {passenger_in.surname} boarded bus {bus.name}")
        
        bus.curr_pos.waiting.difference_update(to_remove)

    def unboard():
        to_remove = set()
        for passenger_out in bus.on_board:

            if passenger_out.planned_trip.to == bus.curr_pos:
                
                to_remove.add(passenger_out)
                passenger_out.on_bus = False
                passenger_out.arrived = True
                passenger_out.curr_stop = bus.curr_pos
                bus.curr_pos.waiting.add(passenger_out)
                passenger_out.bus_he_is_on = None

                logging.info(f"Passenger {passenger_out.name} {passenger_out.surname} left bus {bus.name}")
        bus.on_board.difference_update(to_remove)

    while env.time < STOP_AT:
        for place in stops_to_traverse:

            if env.time >= STOP_AT:
                break

            if isinstance(place, tds.BusStop):
                bus.curr_pos = place

                ## Arrives at stop
                logging.info(f"Bus {bus.name} is arriving at {place.name}")

                board()

                # wait at stop

                unboard()

                logging.info(f"Bus {bus.name} is waiting at {place.name}")
                for _ in range (4): # wait for 1 minute, board 2 times

                    await asyncio.sleep(15)
                    board()


                await asyncio.sleep(1)
                logging.info(f"Bus {bus.name} is leaving {place.name}")

                # leave stop
            elif isinstance(place, tds.RoadConnection):
                # it should get here without awaiting
                bus.curr_pos = place
                distance_left = place.distance
                tenth = distance_left / 10

                logging.info(f"Bus {bus.name} is travelling to {place.name}")

                while distance_left > 0:

                    # travel in increments of 0.1, keep account of traffic
                    to_travel = tenth if distance_left > tenth else distance_left
                    effective_distance = (to_travel / place.trafficFunc(env.time))

                    distance_left -= to_travel

                    # if you know how to break up a string into multiple lines please do, I can't be bothered to look it up.
                    # Gotchu fam.
                    logging.info(f"Bus {bus.name} is travelling {effective_distance:.1f}m to {place.name},\
                                 {distance_left:.1f}m left, ETA: {(distance_left / bus.speed):.2f}s {1 / place.trafficFunc(env.time):.2f}\
                                 traffic at {place.name}")

                    await asyncio.sleep(effective_distance / bus.speed)

                await asyncio.sleep(1)

        stops_to_traverse.reverse()

        await asyncio.sleep(1)


async def passenger_spawner(env: SimulationEnv):
    def spawn() -> tds.Passenger:
        passenger = tds.Passenger(
            name = names.get_first_name(),
            surname = names.get_last_name(),
            uses_our_app = random.random() < PROB_TO_USE_APP,
            on_bus = False,
            planned_trip=None,
            coawaited = False,
        )

        curr = random.choice(env.stops)

        to = random.choice(env.stops)
        while to == curr:
            to = random.choice(env.stops)

        passenger.planned_trip = tds.Trip(curr, to)

        logging.info(f"Passenger {passenger.name} {passenger.surname} spawned")
        logging.info(f"Passenger {passenger.name} {passenger.surname} is in {curr} and wants to go from {passenger.planned_trip.start.name} to {passenger.planned_trip.to.name}")

        return passenger

    SPAWN_PROBABILITY = 1.00

    logging.info("Passenger spawner starting")

    while env.time < STOP_AT:
        if random.random() < SPAWN_PROBABILITY:

            SPAWN_PROBABILITY = 0.25 # first wave always spawns
            new_passengers = [spawn() for _ in range(random.randint(10, 150))]

            if len(env.passengers) + len(new_passengers) < MAX_PASSENGERS_IN_SIM:
                env.passengers.extend(new_passengers)

                [  # spawn passenger coroutines, but don't await them (they will run in the background)
                    asyncio.create_task(simulate_passenger(passenger, env))
                    for passenger in new_passengers
                ]

                logging.info(f"Spawned new wave of {len(new_passengers)} passengers")
            else:
                logging.warning("Too many passengers, spawn wave throttled")

        await asyncio.sleep(60)


async def simulate_passenger(passenger: tds.Passenger, env: SimulationEnv):
    passenger.planned_trip.start.waiting.add(passenger)

    while env.time < STOP_AT:

        # passengers tick every 15 seconds, checking what their state is
        # they get carried by the bus, so they don't need to do anything
        # by themselves, but they might perform some actions when they
        # tick, such as reporting or despawning

        if passenger.on_bus:

            if len(passenger.bus_he_is_on.on_board) >= (OVERCROWDING_THRESHOLD * passenger.bus_he_is_on.capacity) and not passenger.reported_overcrowding:
                passenger.reported_overcrowding = True
                logging.warning(f"Passenger {passenger.name} {passenger.surname} reports overcrowding on bus {passenger.bus_he_is_on.name}")

        elif passenger.arrived:

            # nice!

            logging.info(f"Passenger {passenger.name} {passenger.surname} leaves the simulation, say bye!")

            env.passengers.remove(passenger)

            break # despawn
        await asyncio.sleep(15+random.random()) # scrambling the wait to avoid huge chunks of work at once


async def main():
    buses = [
        tds.Bus("A-1", 15, 80, routes[0], 0),
        tds.Bus("B-1", 15, 80, routes[0], -1),
    ]

    env = SimulationEnv(bus_stops, routes, buses, [])

    await asyncio.gather(
        tick_time(env),
        passenger_spawner(env),
        *[simulate_bus(bus, env) for bus in buses],
    )

    #env.save("sim.json")


if __name__ == "__main__":

    logging.info("log separator\n\n\n---Starting simulation---\n\n")

    asyncio.run(main())

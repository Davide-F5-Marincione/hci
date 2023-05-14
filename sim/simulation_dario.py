import simpy
import networkx as nx
import random
import matplotlib.pyplot as plt
from dataclasses import dataclass
from typing import List, Dict, Tuple, Callable, Optional
from itertools import chain


@dataclass
class BusStop:
    name: str
    waiting: int

    def __hash__(self):
        return hash(self.name)

    def __eq__(self, other):
        return self.name == other.name

    def __str__(self):
        return self.name

    def __repr__(self):
        return self.name


@dataclass
class Bus:
    name: str
    capacity: int
    speed: float
    route: list


@dataclass
class Route:
    name: str
    stops: List[BusStop]

    def to_edges(self) -> List[Tuple[BusStop, BusStop]]:
        """`to_edges` Converts the route to a list of edges (BusStop, BusStop)

        Returns
        -------
        List[Tuple[BusStop, BusStop]]
            List of edges (BusStop, BusStop)
        """
        edges = []
        for i in range(len(self.stops) - 1):
            edges.append((self.stops[i], self.stops[i + 1]))
        return edges


@dataclass
class Passenger:
    name: str
    origin: BusStop
    destination: BusStop
    wait_time: int
    travel_time: int


routes = [
    Route(
        "310",
        [
            BusStop("Augustus", 0),
            BusStop("Flavia", 0),
            BusStop("Palatina", 0),
            BusStop("Caelina", 0),
            BusStop("Claudia", 0),
            BusStop("Quirina", 0),
            BusStop("Termini", 0),
            BusStop("Capitolina", 0),
            BusStop("Esquilina", 0),
            BusStop("Aventina", 0),
        ],
    ),
    Route(
        "546",
        [
            BusStop("Appia", 0),
            BusStop("Neronia", 0),
            BusStop("Tiberia", 0),
            BusStop("Capitolina", 0),
            BusStop("Aemilia", 0),
            BusStop("Termini", 0),
            BusStop("Horatia", 0),
            BusStop("Valeria", 0),
            BusStop("Pompeia", 0),
            BusStop("Laelia", 0),
        ],
    ),
    Route(
        "913",
        [
            BusStop("Aurelia", 0),
            BusStop("Cornelia", 0),
            BusStop("Julia", 0),
            BusStop("Termini", 0),
            BusStop("Livia", 0),
            BusStop("Octavia", 0),
            BusStop("Poppaea", 0),
            BusStop("Agrippina", 0),
            BusStop("Domitia", 0),
            BusStop("Messalina", 0),
        ],
    ),
]


nodes = set(chain.from_iterable([route.stops for route in routes]))

bus_graph = nx.Graph()
bus_graph.add_nodes_from(nodes)

edges = list(chain.from_iterable([route.to_edges() for route in routes]))

bus_graph.add_edges_from(edges)

# set up SimPy simulation

env = simpy.Environment()

# set up bus stops

bus_stops: Dict[BusStop, simpy.Resource] = {
    node: simpy.Resource(env, capacity=1) for node in nodes
}

# set up buses

buses: List[Bus] = [
    Bus("Bus 1", 30, 1, routes[0].stops),
    Bus("Bus 2", 30, 1, routes[1].stops),
    Bus("Bus 3", 30, 1, routes[2].stops),
]

# set up passengers

passengers: List[Passenger] = chain.from_iterable(
    [
        [
            Passenger(f"Passenger {i}", routes[0].stops[0], routes[0].stops[9], 0, 0)
            for i in range(10)
        ],
        [
            Passenger(f"Passenger {i}", routes[1].stops[0], routes[1].stops[9], 0, 0)
            for i in range(10)
        ],
        [
            Passenger(f"Passenger {i}", routes[2].stops[0], routes[2].stops[9], 0, 0)
            for i in range(10)
        ],
    ]
)


# set up bus processes
def bus(env: simpy.Environment, bus: Bus):
    """`bus` SimPy process for a bus

    Parameters
    ----------
    env : simpy.Environment
        SimPy environment
    bus : Bus
        Bus to be simulated
    """
    while True:
        for stop in bus.route:
            yield env.timeout(bus.speed)
            print(f"Bus {bus.name} arrived at {stop.name}")
            yield env.timeout(stop.waiting)
            print(f"Bus {bus.name} departed {stop.name}")
        yield env.timeout(bus.speed)

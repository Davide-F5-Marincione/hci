import matplotlib.pyplot as plt
from dataclasses import dataclass, field
from typing import List, Dict, Tuple, Callable, Union, Any, Optional, Set
import numpy as np
import random
import json



class SimplexNoiseTraffic:
    """Simplex noise generator

    This is a simplex noise generator, it is used to generate a random value between 0.0 and 1.2,
    the value oscillates as a function of time, but it is not periodic.

    Internally, it is used to simulate traffic jams, but it can be used for other purposes.
    """

    def __init__(self, seed: int = 42):
        """__init__ Initializes the simplex noise generator

        Parameters
        ----------
        seed : int, optional
            The seed for the random number generator, by default 42
        """
        np.random.seed(seed)
        self.coeffs = np.random.rand(3)
        self.coeffs = (
            self.coeffs * [np.sqrt(2), np.exp(1), np.pi] / self.coeffs.sum()
        )

        # scale for longer periods

        self.coeffs = self.coeffs * 0.02

        self.func = lambda i: 1 / 5 * np.sin(self.coeffs * i).sum() + 0.6

        self.i: float = 0.0
        self.step: float = 0.1

    def __call__(self, t: Union[float, None] = None):
        """__call__ Calls the simplex noise generator

        Parameters
        ----------
        t : float, optional
            A time value, if not given it will step an internal counter, by default None

        Returns
        ------
        float
            a traffic coefficient, between 0. and 1.2
        """
        if t is None:
            retval = self.func(self.i)
            self.i += self.step

        else:
            retval = self.func(t)

        return retval


class SimplexNoiseBounded:
    """Simplex noise generator

    This is a simplex noise generator, it is used to generate a random value between 0. and 1.,
    the value oscillates as a function of time, but it is not periodic.

    It might be used to enrich the simulation with random events.
    """

    def __init__(self, seed: int = 42):
        """__init__ Initializes the simplex noise generator

        Parameters
        ----------
        seed : int, optional
            The seed for the random number generator, by default 42
        """
        np.random.seed(seed)
        self.coeffs = np.random.rand(3)
        self.coeffs = (
            self.coeffs * [np.sqrt(2), np.exp(1), np.pi] / self.coeffs.sum()
        )

        self.func = lambda i: 1 / 6 * np.sin(self.coeffs * i).sum() + 0.5

        self.i: float = 0.0
        self.step: float = 0.1

    def __call__(self, t: Union[float, None] = None):
        """__call__ Calls the simplex noise generator

        Parameters
        ----------
        t : float, optional
            A time value, if not given it will step an internal counter, by default None

        Returns
        ------
        float
            a traffic coefficient, between 0. and 1.
        """
        if not t:
            retval = self.func(self.i)
            self.i += self.step

        else:
            retval = self.func(t)

        return retval

@dataclass
class Position:

    x: float
    y: float

@dataclass
class BusStop(Position):
    name: str
    waiting: Set[Any]
    buses: Set[Any] = field(default_factory=set)


    def __hash__(self):
        return hash(self.__repr__())

    def __eq__(self, other):
        return self.__hash__() == other.__hash__()

    def __str__(self):
        return self.name

    def __repr__(self):
        return self.name

def distance_between_bus_stops(stop1: BusStop, stop2: BusStop) -> float:

    return np.linalg.norm(np.array([stop1.x, stop1.y]) - np.array([stop2.x, stop2.y]))


@dataclass
class RoadConnection(Position):
    fromStop: BusStop
    toStop: BusStop
    distance: float = 0.0
    trafficFunc: Callable[[float], float] = field(
        default_factory=SimplexNoiseTraffic
    )

    def __post_init__(self):
        self.distance = distance_between_bus_stops(self.fromStop, self.toStop)
        self.name = f"{self.fromStop.name} <-> {self.toStop.name}"

        self.x = (self.fromStop.x + self.toStop.x) / 2
        self.y = (self.fromStop.y + self.toStop.y) / 2

    def __repr__(self) -> str:
        return self.name

    def __hash__(self):
        return hash(self.__repr__())


class Route:
    name: str
    stops: List[Union[BusStop, RoadConnection]]

    def __init__(self, name: str, stops: List[BusStop]):
        self.name = name
        self.stops: List[Union[BusStop, RoadConnection]] = self.introduce_roads(
            stops
        )

        self._edges_list = self._to_edges_from_connections()

    def __str__(self):
        """Return a string representation of this Route"""
        start = self.stops[0]
        end = self.stops[-1]
        return f"""Route {self.name}\n{'-'*(len(self.name+'Route ') + 2 )}\n{len(self.stops)} stops
start: {start}\nend: {end}\n{self.stops}\n{'-'*(len(self.name+'Route ') + 2 )}"""

    def __repr__(self):
        """__repr__ JSON representation of this Route"""
        return json.dumps(
            {
                "name": self.name,
                "stops": [str(s) for s in self.stops],
            }
        )

    def __hash__(self):
        return hash(self.__repr__())

    def __eq__(self, other):
        return self.__repr__() == other.__repr__()

    def __len__(self):
        return len(self.stops)

    def __getitem__(self, key):
        return self.stops[key]

    def __iter__(self):
        return iter(self.stops)

    def introduce_roads(
        self, stops: List[BusStop]
    ) -> List[Union[BusStop, RoadConnection]]:
        """introduce_roads Introduces roads between stops

        Parameters
        ----------
        stops : List[BusStop]
            A list of BusStops

        Returns
        -------
        List[Union[BusStop, RoadConnection]]
            A list of BusStops and RoadConnections
        """
        stops_populated = [stops[0]]

        for i in range(1, len(stops)):
            stops_populated.append(
                RoadConnection(
                    0,
                    0,
                    stops[i - 1],
                    stops[i],
                    SimplexNoiseTraffic(seed=(i + random.randint(0, 1024))),
                )
            )

            stops_populated.append(stops[i])

        return stops_populated

    def __post_init__(self):
        # ensure that stops is a well-formed bipartite chain graph

        if len(self.stops) <= 1:
            raise ValueError("Route must have at least two stops")

        if not isinstance(self.stops[0], BusStop):
            raise ValueError("First stop must be a BusStop")

        if not isinstance(self.stops[-1], BusStop):
            raise ValueError("Last stop must be a BusStop")

        for i in range(len(self.stops) - 1):
            if isinstance(self.stops[i], BusStop) and isinstance(
                self.stops[i + 1], BusStop
            ):
                raise ValueError("Two consecutive stops cannot be BusStops")

            if isinstance(self.stops[i], RoadConnection) and isinstance(
                self.stops[i + 1], RoadConnection
            ):
                raise ValueError(
                    "Two consecutive stops cannot be RoadConnections"
                )

    def to_edges(
        self, seed=None
    ) -> List[
        Union[Tuple[BusStop, RoadConnection], Tuple[RoadConnection, BusStop]]
    ]:
        """`to_edges` Converts the route to a list of edges (BusStop, RoadConnection) or (RoadConnection, BusStop)

        Returns
        -------
        List[Union[Tuple[BusStop, RoadConnection], Tuple[RoadConnection, BusStop]]]
            List of edges (BusStop, RoadConnection) or (RoadConnection, BusStop)
        """

        return self._edges_list

    def _to_edges_from_connections(
        self,
    ) -> List[Tuple[RoadConnection, BusStop]]:
        edges = []

        for i in range(len(self.stops) - 1):
            edges.append((self.stops[i], self.stops[i + 1]))

        return edges

@dataclass
class Bus:
    name: str
    capacity: int
    speed: float
    route: Route
    curr_pos_idx: int
    curr_pos: Position = field(init=False, repr=False) # type: Union[BusStop, RoadConnection]
    on_board: Set[Any] = field(default_factory=set)
    next_idx: int = None
    # Fastfix
    last_stop: str = ""
    next_stop: str = ""
    departure_time: float = 0.0

    def __post_init__(self):

        assert abs(self.curr_pos_idx) < len(self.route), "curr_pos_idx must be less than |len(route)|"
        assert self.capacity > 0, "capacity must be greater than 0"
        assert self.speed > 0, "speed must be greater than 0"
        assert isinstance(self.route[self.curr_pos_idx], BusStop), f"curr_pos_idx must be a BusStop index, instead it is {type(self.route[self.curr_pos_idx])}"


    def __hash__(self):
        return hash(self.__repr__())

    def __repr__(self):
        return f"Bus {self.name}, capacity: {self.capacity}, speed: {self.speed}"

@dataclass
class Trip:

    start: BusStop # from is reserved
    to: BusStop

@dataclass
class Passenger:
    name: str
    surname: str
    uses_our_app: bool

    bus_he_is_on: Optional[Bus] = None
    reported_overcrowding: bool = False
    reported_boarding: bool = False
    can_report: bool = False
    arrived: bool = False


    def __hash__(self):
        return hash(self.__repr__())

    def __eq__(self, other):
        return self.__repr__() == other.__repr__()


if __name__ == "__main__":

    stops = [
        BusStop(2.0, -1.0, "A", set()),
        BusStop(1.5, 4.0, "B", set()),
        BusStop(-0.5, 1.0, "C", set()),
        BusStop(-2.0, 1.5, "D", set()),
    ]

    route = Route(
        "A",
        stops,
    )

    print(route)

    for edge in route.to_edges():
        print(edge)

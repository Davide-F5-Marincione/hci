from dataclasses import dataclass, field
from typing import List, Tuple, Dict, Any, Optional
import random
import time
import numpy as np
import cv2
import datetime
import math

MIN = 0
MAX = 1
ADD = 0
WAITING_SECONDS = 20
SPEED = 20
CAPACITY = 50
MAX_OVERCROWD_TIME_AGO = 1024
TELEPORT = True

ALPHA = .84
BETA = .16

PROB_TO_USE_APP = .2
SIGNAL_PROB = .3
FALLOFF = 0.02
SHARPNESS = 20
UNCERTAINTY = .7

TIME_TO_RECHARGE = 120

EXPECTED_SPEED = ((MAX + MIN) / 2 + ADD) * SPEED
AVG_SPEED = (MAX + MIN) / 2 * SPEED

SIMPLEX_GRANULARITY = 1

def simplex_noise(seed):
    np.random.seed(seed)
    vals = np.random.rand(3) * 15 + 10
    vals = vals * [np.sqrt(2), np.exp(1), np.pi] / vals.sum()
    i = 0
    while True:
        i += 1
        yield (1 / 6 * np.sin(vals * i / SIMPLEX_GRANULARITY).sum() + 0.5)

def fixed_sigmoid(fill, max):
    sigm = lambda x: 1/(1 + math.exp(UNCERTAINTY * SHARPNESS - x * SHARPNESS))
    return (sigm(fill / max) - sigm(0)) / (sigm(1) - sigm(0))

@dataclass
class Node:
    name: str
    x: float
    y: float


@dataclass
class Edge:
    a: Node
    b: Node
    traffic_update: iter
    curr_traffic: float = (MAX + MIN) / 2

    def length(self):
        return ((self.a.x - self.b.x) ** 2 + (self.a.y - self.b.y) ** 2) ** 0.5

    def expected_steps(self):
        return self.length() / EXPECTED_SPEED

    def step(self):
        self.curr_traffic = next(self.traffic_update)

@dataclass
class Passenger:
    is_user: bool
    time_to_recharge: float = 0.0
    on_for: float = 0.0
    
@dataclass
class TrueBus:
    name: str
    route: str
    prev_node: str
    next_node: str
    dir: int = 1
    distance_travelled: float = 0.0
    capacity: int = CAPACITY
    waiting: float = WAITING_SECONDS
    time_passed: float = 0.0
    boarded: bool = False
    delay: float = 0.0
    passengers: List[Passenger] = field(default_factory=lambda: [])

    def step(self, graph, routes, v_buses, delta_t=1):

        if self.waiting > delta_t:  # Just wait
            self.waiting -= delta_t
        else:  # Board calc
            if not self.boarded:
                max_board = self.capacity - len(self.passengers)
                general_board = random.randint(0, max_board)

                for _ in range(general_board):
                    self.passengers.append(Passenger(np.random.rand() < PROB_TO_USE_APP))

                self.waiting = 0.0
                
                self.boarded = True

            conn = graph.get_edge(self.prev_node, self.next_node)
            self.distance_travelled += (conn.curr_traffic * (MAX - MIN) + MIN) * delta_t * SPEED
            self.time_passed += delta_t

            p_to_signal_overcrowding = fixed_sigmoid(len(self.passengers), self.capacity)
            for passenger in self.passengers:
                if passenger.is_user:
                    p_to_signal_whatever = SIGNAL_PROB / (FALLOFF * passenger.on_for + 1)
                    if passenger.time_to_recharge > 0.0:
                        passenger.time_to_recharge -= delta_t
                    else:
                        p_overcrowding = p_to_signal_whatever * p_to_signal_overcrowding
                        p_not_overcrowding = 1 - p_to_signal_whatever * (1 - p_to_signal_overcrowding)
                        r = random.random()
                        if r < p_overcrowding:
                            v_buses[self.name].overcrowd_signal(True, self)
                            passenger.time_to_recharge = TIME_TO_RECHARGE
                        elif r > p_not_overcrowding:
                            v_buses[self.name].overcrowd_signal(False, self)
                            passenger.time_to_recharge = TIME_TO_RECHARGE
                passenger.on_for += delta_t

            if self.distance_travelled >= conn.length():  # Arrived at next stop
                # Reset
                ahead = conn.expected_steps() - self.time_passed + WAITING_SECONDS
                diff = max(0, ahead - WAITING_SECONDS)
                self.waiting = WAITING_SECONDS + max(0, diff - self.delay)
                self.delay = max(0, self.delay - diff)

                self.distance_travelled = 0.0
                self.time_passed = 0
                self.boarded = False

                # Unload passengers
                general_unload = random.randint(0, len(self.passengers))
                random.shuffle(self.passengers)
                self.passengers = self.passengers[general_unload:]

                # Prepare stop
                circ = routes[self.route].circuit[::self.dir]
                self.prev_node = self.next_node
                for i, node in enumerate(circ):
                    if node == self.prev_node:
                        if i == len(circ) - 1:
                            self.next_node = circ[0]
                        else:
                            self.next_node = circ[i + 1]
                        break

        v_buses[self.name].step(graph, routes, delta_t)


@dataclass
class VirtualBus:
    name: str
    route: str
    prev_node: str
    next_node: str
    dir: int = 1
    distance_travelled: float = 0.0
    waiting: float = WAITING_SECONDS
    time_passed: float = 0.0
    mu_overcrowded: float = 0.5
    var_overcrowded: float = 0.1
    last_signal: datetime.datetime = datetime.datetime.fromtimestamp(0)
    speed: float = EXPECTED_SPEED
    delay: float = 0.0
    
    def overcrowd_signal(self, is_overcrowded, bus):
        self.last_signal = datetime.datetime.now()
        sample = 1 if is_overcrowded else 0
        new_mu = ALPHA * sample + (1 - ALPHA) * self.mu_overcrowded
        self.var_overcrowded = BETA * (sample - self.mu_overcrowded) ** 2 + (1 - BETA) * self.var_overcrowded
        self.mu_overcrowded = new_mu

        self.prev_node = bus.prev_node
        self.next_node = bus.next_node
        self.distance_travelled = bus.distance_travelled
        self.delay += self.time_passed - bus.time_passed
        self.time_passed = self.distance_travelled / self.speed
        self.waiting = 0

    def step(self, graph, routes, delta_t=1):
        if self.waiting > 0:  # Just wait
            self.waiting -= delta_t
        else:  # Move
            conn = graph.get_edge(self.prev_node, self.next_node)
            self.distance_travelled += self.speed * delta_t
            self.time_passed += delta_t

            if self.distance_travelled >= conn.length():  # Arrived at next stop
                # Reset
                self.waiting = max(
                    WAITING_SECONDS, conn.expected_steps() - self.time_passed
                )
                self.distance_travelled = 0.0
                self.time_passed = 0
                self.speed = EXPECTED_SPEED

                # Prepare stop
                circ = routes[self.route].circuit[::self.dir]
                self.prev_node = self.next_node
                for i, node in enumerate(circ):
                    if node == self.prev_node:
                        if i == len(circ) - 1:
                            self.next_node = circ[0]
                        else:
                            self.next_node = circ[i + 1]
                        break

@dataclass
class Route:
    name: str
    color: Tuple[int, int, int]
    circuit: List[str]
    transit_type: str


@dataclass
class Graph:
    stops: Dict[str, Node]
    edges: Dict[str, Edge]

    def startup(self, stops):
        self.stops = {
            stop: Node(stop, random.randint(-10, 10), random.randint(-10, 10))
            for stop in stops
        }
        self.edges = dict()

    def get_edge(self, a, b):
        if (edge := self.edges.get((a, b), None)) is not None:
            return edge
        return self.edges.get((b, a), None)

    def add_edge(self, a, b):
        if (edge := self.get_edge(a, b)) is not None:
            return edge
        else:
            edge = Edge(
                self.stops[a], self.stops[b], simplex_noise(random.getrandbits(32))
            )
            self.edges[(a, b)] = edge
            return edge
        
@dataclass
class SearchBus:
    name: str
    route: str
    prev_node: str
    next_node: str
    dir: int = 1
    distance_travelled: float = 0.0
    waiting: float = WAITING_SECONDS
    time_passed: float = 0.0
    speed: float = EXPECTED_SPEED

    def step(self, graph, routes):
        if self.waiting > 0:  # Just wait
            self.waiting -= 1
        else:  # Move
            conn = graph.get_edge(self.prev_node, self.next_node)
            self.distance_travelled += self.speed
            self.time_passed += 1

            if self.distance_travelled >= conn.length():  # Arrived at next stop
                # Reset
                self.waiting = max(
                    WAITING_SECONDS, conn.expected_steps() - self.time_passed
                )
                self.distance_travelled = 0.0
                self.time_passed = 0
                self.speed = EXPECTED_SPEED

                # Prepare stop
                circ = routes[self.route].circuit[::self.dir]
                self.prev_node = self.next_node
                for i, node in enumerate(circ):
                    if node == self.prev_node:
                        if i == len(circ) - 1:
                            self.next_node = circ[0]
                        else:
                            self.next_node = circ[i + 1]
                        break

@dataclass
class AtStop:
    name: str
    time: int
    children: List[Any]
    parent: Optional[Any]
    to_find: str

    def check(self, graph, buses, reached, time):

        for child in self.children:
            child.check(graph, buses, reached, time)

        for bus in buses:
            if reached["bus-" + bus.name] is None and bus.waiting > 0 and bus.prev_node == self.name:
                child = OnTravel(bus.name, time, bus, dict(), self, [(self.name, time)], self.to_find)
                self.children.append(child)
                reached["bus-" + bus.name] = child
@dataclass
class OnTravel:
    name: str
    time: str
    bus: SearchBus
    children: Dict[str, AtStop]
    parent: AtStop
    stops: List[Tuple[str, int]]
    to_find: str
    found: bool = False

    def check(self, graph, buses, reached, time):
        if self.found:
            return

        for child in self.children.values():
            child.check(graph, buses, reached, time)

        if self.bus.waiting > 0:
            if self.stops[-1][0] != self.bus.prev_node:
                self.stops.append((self.bus.prev_node, time))

                if self.children.get(self.bus.prev_node, None) is None:
                    child = AtStop(self.bus.prev_node, time, [], self, self.to_find)
                    self.children[self.bus.prev_node] = child

                    reached["stop-" + self.bus.prev_node].append(child)

                if self.bus.prev_node == self.to_find:
                    self.found = True

def directions(start, end, graph, routes, vbuses):
    # Deep-copy buses
    buses = [SearchBus(bus.name, bus.route, bus.prev_node, bus.next_node, bus.dir, bus.distance_travelled, bus.waiting, bus.time_passed, bus.speed) for bus in vbuses.values()]
    
    seconds_passed = 0
    tree = AtStop(start, seconds_passed, [], None, end)
    reached = {"bus-" + bus.name: None for bus in buses}
    reached.update({"stop-" + stop.name: [] for stop in graph.stops.values()})
    reached["stop-" + start].append(tree)

    first_found = False
    max_time = 3600
    while seconds_passed < max_time:
        tree.check(graph, buses, reached, seconds_passed)

        if len(reached["stop-" + end]) > 1 and not first_found:
            first_found = True
            max_time = seconds_passed + 1800

        if len(reached["stop-" + end]) >= 3:
            max_time = 0

        for bus in buses:
            bus.step(graph, routes)

        seconds_passed += 1

    if len(reached["stop-" + end]) < 1:
        print("Couldn't find route")
        return None
    else:
        results = []
        for elem in reached["stop-" + end]:
            unravel = []
            while elem is not None:
                unravel.append(elem)
                elem = elem.parent

            lst = unravel[::-1]

            foo = []

            i = 0
            while i < len(lst) - 1:
                board = lst[i]
                bus = lst[i+1]
                unboard = lst[i+2]
                i = i+2

                found_board = False
                bar = []

                for place, time in bus.stops:
                    if not found_board:
                        if place == board.name:
                            bar.append((place, time))
                            found_board = True
                    else:
                        bar.append((place, time))
                        if place == unboard.name:
                            break
                foo.append((bus.name, bar))
            results.append(foo)
        return results
            


def shrink(x):
    return int(x/10)

if __name__ == "__main__":
    stops = ["A", "B", "C", "D", "E", "F", "G"]
    graph = Graph(dict(), dict())
    graph.startup(stops)
    graph.stops["A"].x = 3000
    graph.stops["A"].y = 3000
    graph.stops["B"].x = 4000
    graph.stops["B"].y = 3000
    graph.stops["C"].x = 4000
    graph.stops["C"].y = 4000
    graph.stops["D"].x = 3000
    graph.stops["D"].y = 4000
    graph.stops["E"].x = 4500
    graph.stops["E"].y = 4750
    graph.stops["F"].x = 3500
    graph.stops["F"].y = 4750
    graph.stops["G"].x = 2500
    graph.stops["G"].y = 4750

    v_buses = { "A1": VirtualBus("A1", "A", "A", "B"),
                "A3": VirtualBus("A3", "A", "C", "D"),
                "A2": VirtualBus("A2", "A", "D", "C", -1),
                "B1": VirtualBus("B1", "B", "C", "E"),
                "B2": VirtualBus("B2", "B", "F", "E", -1),
                "C1": VirtualBus("C1", "C", "D", "G"),
                "C2": VirtualBus("C2", "C", "F", "G", -1)}

    buses = {   "A1": TrueBus("A1", "A", "A", "B"),
                "A3": TrueBus("A3", "A", "C", "D"),
                "A2": TrueBus("A2", "A", "D", "C", -1),
                "B1": TrueBus("B1", "B", "C", "E"),
                "B2": TrueBus("B2", "B", "F", "E", -1),
                "C1": TrueBus("C1", "C", "D", "G"),
                "C2": TrueBus("C2", "C", "F", "G", -1)}

    routes = {"A": Route("A", (205, 92, 92), ["A", "B", "C", "D"]),
              "B": Route("B", (0, 49, 83), ["C", "E", "F"]),
              "C": Route("C", (1,68,33), ["D", "G", "F"])}
    
    for route in routes.values():
        this_buses = []

        first = prev = route.circuit[0]
        for curr in route.circuit[1:]:
            graph.add_edge(prev, curr)
            prev = curr
        graph.add_edge(prev, first)

    title = "Simulation"
    v_title = "Virtual buses"

    img = np.ones((800, 800, 3), dtype=np.uint8) * 255

    run = True

    cv2.namedWindow(title)
    cv2.namedWindow(v_title)

    last_time = time.time()

    while run:
        buff_img = img.copy()

        for route in routes.values():
            start = prev = graph.stops[route.circuit[0]]

            for curr in route.circuit[1:]:
                curr = graph.stops[curr]
                cv2.line(buff_img, (shrink(prev.x), shrink(prev.y)), (shrink(curr.x), shrink(curr.y)), route.color, 3)
                prev = curr
            cv2.line(buff_img, (shrink(prev.x), shrink(prev.y)), (shrink(start.x), shrink(start.y)), route.color, 3)

        for node in graph.stops.values():
            cv2.circle(buff_img, (shrink(node.x), shrink(node.y)), 5, (12,12,12), 3)

        v_buff_img = buff_img.copy()

        for bus in buses.values():
            start = graph.stops[bus.prev_node]
            stop = graph.stops[bus.next_node]
            conn = graph.get_edge(bus.prev_node, bus.next_node)
            l = conn.length()
            dir_x = (stop.x - start.x) / l * bus.distance_travelled
            dir_y = (stop.y - start.y) / l * bus.distance_travelled
            pos_x = dir_x + start.x
            pos_y = dir_y + start.y

            col = (255, 255, 0)

            cv2.circle(buff_img, (shrink(pos_x), shrink(pos_y)), 5, col, -1)
            cv2.putText(buff_img, bus.name, (shrink(pos_x) + 5, shrink(pos_y)), fontFace=0, fontScale=.4, color=(12,12,12))

        for bus in v_buses.values():
            start = graph.stops[bus.prev_node]
            stop = graph.stops[bus.next_node]
            conn = graph.get_edge(bus.prev_node, bus.next_node)
            l = conn.length()
            dir_x = (stop.x - start.x) / l * bus.distance_travelled
            dir_y = (stop.y - start.y) / l * bus.distance_travelled
            pos_x = dir_x + start.x
            pos_y = dir_y + start.y

            col = (255, 255, 0)

            cv2.circle(v_buff_img, (shrink(pos_x), shrink(pos_y)), 5, col, -1)
            cv2.putText(v_buff_img, bus.name, (shrink(pos_x) + 5, shrink(pos_y)), fontFace=0, fontScale=.4, color=(12,12,12))

        cv2.imshow(title, buff_img)
        cv2.imshow(v_title, v_buff_img)

        key = cv2.waitKey(10)

        if key == ord("q"):
            run = False

        time_now = time.time()
        time_delta = time_now - last_time

        last_time = time_now

        for bus in buses.values():
            bus.step(graph, buses, v_buses, time_delta)

        for edge in graph.edges.values():
            edge.step()

    cv2.destroyAllWindows()
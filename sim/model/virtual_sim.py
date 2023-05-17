from dataclasses import dataclass
from typing import List, Tuple, Dict, Any, Optional
import numpy as np
import random
import cv2
import time

MIN = 0
MAX = 1.2
WAITING_SECONDS = 20
SPEED = 100
MAX_OVERCROWD_TIME_AGO = 1024
TELEPORT = True

PROB_TO_USE_APP = 0.2
PROB_TO_UNBOARD = .3

SIGN_PROB = 0.5

OVERCROWDING_THRESHOLD = 0.8
MIN_OVERCROWD_PROB = .4
MAX_OVERCROWD_PROB = .8

FALLOFF = 0.02

SPEED = 200
CAPACITY = 50

def simplex_noise(seed):
    np.random.seed(seed)
    vals = np.random.rand(3) * 15 + 10
    vals = vals * [np.sqrt(2), np.exp(1), np.pi] / vals.sum()
    i = 0
    while True:
        i += 1
        yield (1 / 6 * np.sin(vals * i / 50).sum() + 0.5)

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
        return self.length() / ((MAX + MIN) / 2 * SPEED)

    def step(self):
        self.curr_traffic = next(self.traffic_update)
    
@dataclass
class TrueBus:
    name: str
    route: str
    prev_node: str
    next_node: str
    dir: int = 1
    distance_travelled: float = 0.0
    capacity: int = CAPACITY
    fill: int = 0
    users: int = 0
    waiting: float = WAITING_SECONDS
    steps_run: float = 0.0
    curr_signaled: bool = False
    boarded: bool = False

    def step(self, graph, routes, buses, v_buses, delta_t=1):
        if self.waiting > delta_t:  # Just wait
            self.waiting -= delta_t
        else:  # Board calc
            if not self.boarded:
                max_board = self.capacity - self.fill
                general_board = random.randint(0, max_board)
                users_board = 0
                for _ in range(general_board):
                    if np.random.rand() < PROB_TO_USE_APP:
                        users_board += 1

                self.fill += general_board
                self.users += users_board
                self.waiting = 0.0
                
                self.boarded = True

            conn = graph.get_edge(self.prev_node, self.next_node)
            self.distance_travelled += (conn.curr_traffic * (MAX - MIN) + MIN) * delta_t * SPEED
            self.steps_run += delta_t

            if self.curr_signaled is False:
                p = SIGN_PROB / (FALLOFF * self.steps_run + 1)
                p = 1 - (1 - p)**self.users
                if np.random.rand() < p:
                    v_buses[self.name].board_signal(
                        self.prev_node, self.next_node, graph, self.steps_run
                    )
                    self.curr_signaled = True
                
            if self.fill >= OVERCROWDING_THRESHOLD * self.capacity:
                p = (self.fill / self.capacity - OVERCROWDING_THRESHOLD) / (1 - OVERCROWDING_THRESHOLD) * (MAX_OVERCROWD_PROB - MIN_OVERCROWD_PROB) + MIN_OVERCROWD_PROB
                p = p / (FALLOFF * self.steps_run + 1)
                p = 1 - (1 - p)**self.users
                if random.random() < p:
                    v_buses[self.name].overcrowd()

            if self.distance_travelled >= conn.length():  # Arrived at next stop
                # Reset
                self.waiting = WAITING_SECONDS
                self.distance_travelled = 0.0
                self.steps_run = 0
                self.curr_signaled = False
                self.boarded = False

                # Unload passengers
                general_unload = random.randint(0, self.fill)
                users_unload = random.randint(
                    max(0, general_unload - (self.fill - self.users)),
                    min(general_unload, self.users),
                )
                self.fill -= general_unload
                self.users -= users_unload

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

                for bus in buses.values():
                    if (
                        bus.prev_node == self.prev_node
                        and bus.next_node == self.next_node
                    ):
                        if bus.name != self.name and bus.waiting < self.waiting:
                            self.waiting = graph.get_edge(
                                    self.prev_node, self.next_node
                                ).expected_steps() // 4 * 3
                self.waiting -= delta_t
        v_buses[self.name].step(graph, routes, v_buses, delta_t)


@dataclass
class VirtualBus:
    name: str
    route: List[str]
    prev_node: str
    next_node: str
    dir: int = 1
    distance_travelled: float = 0.0
    waiting: float = WAITING_SECONDS
    steps_run: float = 0.0
    overcrowded: float = MAX_OVERCROWD_TIME_AGO
    speed: float = (MAX + MIN) / 2 * SPEED

    def board_signal(self, prev_node, next_node, graph, ago=0):
        if TELEPORT:
            self.waiting = 0
            self.steps_run = ago
            self.prev_node = prev_node
            self.next_node = next_node
            self.distance_travelled = ago * ((MAX + MIN) / 2) * SPEED
        else:
            if self.waiting > 0 or prev_node != self.prev_node:
                self.waiting = 0
                self.steps_run = 0
                self.prev_node = prev_node
                self.next_node = next_node
                self.distance_travelled = 0.0
            conn = graph.get_edge(self.prev_node, self.next_node)
            time = conn.expected_steps() - ago
            self.speed = (conn.length() - self.distance_travelled) / time
    
    def overcrowd(self):
        self.overcrowded = 0

    def step(self, graph, routes, v_buses, delta_t=1):
        self.overcrowded = min(self.overcrowded + delta_t, MAX_OVERCROWD_TIME_AGO)
        if self.waiting > 0:  # Just wait
            self.waiting -= delta_t
        else:  # Move
            conn = graph.get_edge(self.prev_node, self.next_node)
            self.distance_travelled += self.speed * delta_t
            self.steps_run += delta_t

            if self.distance_travelled >= conn.length():  # Arrived at next stop
                # Reset
                self.waiting = WAITING_SECONDS
                self.distance_travelled = 0.0
                self.steps_run = 0
                self.speed = ((MAX + MIN) / 2) * SPEED

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

                for bus in v_buses.values():
                    if (
                        bus.prev_node == self.prev_node
                        and bus.next_node == self.next_node
                    ):
                        if bus.name != self.name and bus.waiting < self.waiting:
                            self.waiting = graph.get_edge(
                                    self.prev_node, self.next_node
                                ).expected_steps() / 4 * 3
                self.waiting -= delta_t

@dataclass
class Route:
    name: str
    color: Tuple[int, int, int]
    circuit: List[str]


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
    steps_run: float = 0.0
    speed: float = (MAX + MIN) / 2 * SPEED

    def step(self, graph, routes, other_buses):
        if self.waiting > 0:  # Just wait
            self.waiting -= 1
        else:  # Move
            conn = graph.get_edge(self.prev_node, self.next_node)
            self.distance_travelled += self.speed
            self.steps_run += 1

            if self.distance_travelled >= conn.length():  # Arrived at next stop
                # Reset
                self.waiting = WAITING_SECONDS
                self.distance_travelled = 0.0
                self.steps_run = 0
                self.speed = ((MAX + MIN) / 2) * SPEED

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

                for bus in other_buses:
                    if (
                        bus.prev_node == self.prev_node
                        and bus.next_node == self.next_node
                    ):
                        if bus.name != self.name and bus.waiting < self.waiting:
                            self.waiting = graph.get_edge(
                                    self.prev_node, self.next_node
                                ).expected_steps() / 4 * 3
                self.waiting -= 1

@dataclass
class AtStop:
    name: str
    time: int
    children: List[Any]
    parent: Optional[Any]

    def check(self, graph, buses, reached, time):

        for child in self.children:
            child.check(graph, buses, reached, time)

        for bus in buses:
            if reached["bus-" + bus.name] is None and bus.waiting > 0 and bus.prev_node == self.name:
                child = OnTravel(bus.name, time, bus, [], self, [(self.name, time)])
                self.children.append(child)
                reached["bus-" + bus.name] = child
@dataclass
class OnTravel:
    name: str
    time: str
    bus: SearchBus
    children: List[AtStop]
    parent: AtStop
    stops: List[Tuple[str, int]]

    def check(self, graph, buses, reached, time):
        for child in self.children:
            child.check(graph, buses, reached, time)

        if self.bus.waiting > 0:
            if self.stops[-1][0] != self.bus.prev_node:
                self.stops.append((self.bus.prev_node, time))

            if reached["stop-" + self.bus.prev_node] is None:
                child = AtStop(self.bus.prev_node, time, [], self)
                self.children.append(child)
                reached["stop-" + self.bus.prev_node] = child

def directions(start, end, graph, routes, vbuses):
    # Deep-copy buses
    buses = [SearchBus(bus.name, bus.route, bus.prev_node, bus.next_node, bus.dir, bus.distance_travelled, bus.waiting, bus.steps_run, bus.speed) for bus in vbuses.values()]
    
    seconds_passed = 0
    tree = AtStop(start, seconds_passed, [], None)
    reached = {"bus-" + bus.name: None for bus in buses}
    reached.update({"stop-" + stop.name: None for stop in graph.stops.values()})
    reached["stop-" + start] = tree

    run = True
    while seconds_passed < 3600 and run:
        tree.check(graph, buses, reached, seconds_passed)

        if reached["stop-" + end] is not None:
            run = False

        for bus in buses:
            bus.step(graph, routes, buses)

        seconds_passed += 1

    if run:
        print("Couldn't find route")
        return None
    else:
        unravel = []
        elem = reached["stop-" + end]
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
        return foo
            


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

    v_buses = { "A1": VirtualBus("A1", ["A", "B", "C", "D", "C", "B"], "A", "B"),
                "A2": VirtualBus("A2", ["C", "D", "C", "B", "A", "B"], "C", "D"),
                "1A": VirtualBus("1A", ["D", "C", "B", "A", "B", "C"], "D", "C"),
                "B1": VirtualBus("B1", ["C", "E", "F", "E"], "C", "E"),
                "1B": VirtualBus("1B", ["F", "E", "C", "E"], "F", "E"),
                "C1": VirtualBus("C1", ["D", "G", "F", "G"], "D", "G"),
                "1C": VirtualBus("1C", ["F", "G", "D", "G"], "F", "G")}

    buses = {   "A1": TrueBus("A1", ["A", "B", "C", "D", "C", "B"], "A", "B"),
                "A2": TrueBus("A2", ["C", "D", "C", "B", "A", "B"], "C", "D"),
                "1A": TrueBus("1A", ["D", "C", "B", "A", "B", "C"], "D", "C"),
                "B1": TrueBus("B1", ["C", "E", "F", "E"], "C", "E"),
                "1B": TrueBus("1B", ["F", "E", "C", "E"], "F", "E"),
                "C1": TrueBus("C1", ["D", "G", "F", "G"], "D", "G"),
                "1C": TrueBus("1C", ["F", "G", "D", "G"], "F", "G")}

    routes = {"A":Route("A", (255, 0, 0), ["A", "B", "C", "D"]),
                "B":Route("B", (0, 255, 0), ["C", "E", "F"]),
                "C":Route("C", (0, 0, 255), ["D", "G", "F"])}
    
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
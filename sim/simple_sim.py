from dataclasses import dataclass
from typing import List, Tuple, Dict
import numpy as np
import random
import pickle
import cv2

MIN = 0.01
MAX = 0.5
ADD = 0.01
WAITING_STEPS = 5
MORE_WAITING = 20
SIGN_PROB = 0.5
GRANULARITY = 50
MAX_OVERCROWD_TIME_AGO = 1024
OVERCROWD_THRESHOLD = .8
MIN_OVERCROWD_PROB = .4
MAX_OVERCROWD_PROB = .8
TELEPORT = True


def simplex_noise(seed):
    np.random.seed(seed)
    vals = np.random.rand(3) * 15 + 10
    vals = vals * [np.sqrt(2), np.exp(1), np.pi] / vals.sum()
    i = 0
    while True:
        i += 1
        yield (1 / 6 * np.sin(vals * i / GRANULARITY).sum() + 0.5) * (MAX - MIN) + MIN


@dataclass
class Node:
    name: str
    x: int
    y: int


@dataclass
class Edge:
    a: Node
    b: Node
    traffic_update: iter
    curr_traffic: float = (MAX + MIN) / 2

    def length(self):
        return ((self.a.x - self.b.x) ** 2 + (self.a.y - self.b.y) ** 2) ** 0.5

    def expected_steps(self):
        return self.length() // ((MAX + MIN) / 2 + ADD) + 1

    def step(self):
        self.curr_traffic = next(self.traffic_update)


@dataclass
class VirtualBus:
    name: str
    route: str
    prev_node: str
    next_node: str
    distance_travelled: float = 0.0
    waiting: int = WAITING_STEPS
    steps_run: int = 0
    overcrowded: int = MAX_OVERCROWD_TIME_AGO
    speed: float = (MAX + MIN) / 2 + ADD

    def board_signal(self, prev_node, next_node, ago=0):
        if TELEPORT:
            self.waiting = 0
            self.steps_run = ago
            self.prev_node = prev_node
            self.next_node = next_node
            self.distance_travelled = ago * ((MAX + MIN) / 2)
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

    def step(self):
        self.overcrowded = min(self.overcrowded + 1, MAX_OVERCROWD_TIME_AGO)
        if self.waiting > 0:  # Just wait
            self.waiting -= 1
        else:  # Move
            conn = graph.get_edge(self.prev_node, self.next_node)
            self.distance_travelled += self.speed
            self.steps_run += 1

            if self.distance_travelled >= conn.length():  # Arrived at next stop
                # Reset
                self.waiting = max(
                    WAITING_STEPS, conn.expected_steps() - self.steps_run
                )
                self.distance_travelled = 0.0
                self.steps_run = 0
                self.speed = (MAX + MIN) / 2 + ADD

                # Prepare stop
                self.prev_node = self.next_node
                for i, node in enumerate(routes[self.route].circuit):
                    if node == self.prev_node:
                        if i == len(routes[self.route].circuit) - 1:
                            self.next_node = routes[self.route].circuit[0]
                        else:
                            self.next_node = routes[self.route].circuit[i + 1]
                        break

                for bus in v_buses.values():
                    if (
                        bus.prev_node == self.prev_node
                        and bus.next_node == self.next_node
                    ):
                        if bus.name != self.name:
                            self.waiting = max(
                                self.waiting,
                                graph.get_edge(
                                    self.prev_node, self.next_node
                                ).expected_steps()
                                // 4
                                * 3,
                            )


@dataclass
class TrueBus:
    name: str
    route: str
    prev_node: str
    next_node: str
    distance_travelled: float = 0.0
    max_capacity: int = 50
    fill: int = 0
    users: int = 0
    waiting: int = WAITING_STEPS
    steps_run: int = 0
    curr_signaled: bool = False

    def step(self):
        if self.waiting > 1:  # Just wait
            self.waiting -= 1
        elif self.waiting == 1:  # Board calc
            max_board = self.max_capacity - self.fill
            general_board = random.randint(0, max_board)
            users_board = random.randint(0, general_board)
            self.fill += general_board
            self.users += users_board
            self.waiting -= 1

            p = SIGN_PROB**self.users
            n = np.random.rand()
            if n > p:
                v_buses[self.name].board_signal(self.prev_node, self.next_node)
                self.curr_signaled = True

            if self.fill >= OVERCROWD_THRESHOLD * self.max_capacity:
                p = (self.fill / self.max_capacity - OVERCROWD_THRESHOLD) / (1 - OVERCROWD_THRESHOLD) * (MAX_OVERCROWD_PROB - MIN_OVERCROWD_PROB) + MIN_OVERCROWD_PROB
                p = p**self.users
                n = np.random.rand()
                if n > p:
                    v_buses[self.name].overcrowd()
        else:  # Move
            conn = graph.get_edge(self.prev_node, self.next_node)
            self.distance_travelled += conn.curr_traffic
            self.steps_run += 1

            if self.curr_signaled is False:
                p = min(0, conn.length() - self.distance_travelled) * SIGN_PROB
                p = p**self.users
                n = np.random.rand()
                if n > p:
                    v_buses[self.name].board_signal(
                        self.prev_node, self.next_node, self.steps_run
                    )
                    self.curr_signaled = True
                
            if self.fill >= OVERCROWD_THRESHOLD * self.max_capacity:
                p = (self.fill / self.max_capacity - OVERCROWD_THRESHOLD) / (1 - OVERCROWD_THRESHOLD) * (MAX_OVERCROWD_PROB - MIN_OVERCROWD_PROB) + MIN_OVERCROWD_PROB
                p = min(0, conn.length() - self.distance_travelled) * p
                p = p**self.users
                n = np.random.rand()
                if n > p:
                    v_buses[self.name].overcrowd()

            if self.distance_travelled >= conn.length():  # Arrived at next stop
                # Reset
                self.waiting = max(
                    WAITING_STEPS, conn.expected_steps() - self.steps_run
                )
                self.distance_travelled = 0.0
                self.steps_run = 0
                self.curr_signaled = False

                # Unload passengers
                general_unload = random.randint(0, self.fill)
                users_unload = random.randint(
                    max(0, general_unload - (self.fill - self.users)),
                    min(general_unload, self.users),
                )
                self.fill -= general_unload
                self.users -= users_unload

                # Prepare stop
                self.prev_node = self.next_node
                for i, node in enumerate(routes[self.route].circuit):
                    if node == self.prev_node:
                        if i == len(routes[self.route].circuit) - 1:
                            self.next_node = routes[self.route].circuit[0]
                        else:
                            self.next_node = routes[self.route].circuit[i + 1]
                        break

                for bus in buses.values():
                    if (
                        bus.prev_node == self.prev_node
                        and bus.next_node == self.next_node
                    ):
                        if bus.name != self.name:
                            self.waiting = max(
                                self.waiting,
                                graph.get_edge(
                                    self.prev_node, self.next_node
                                ).expected_steps()
                                // 4
                                * 3,
                            )


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


random.seed(42)
stops = ["A", "B", "C", "D", "E", "F", "G"]
graph = Graph(dict(), dict())
graph.startup(stops)
graph.stops["A"].x = 300
graph.stops["A"].y = 300
graph.stops["B"].x = 400
graph.stops["B"].y = 300
graph.stops["C"].x = 400
graph.stops["C"].y = 400
graph.stops["D"].x = 300
graph.stops["D"].y = 400
graph.stops["E"].x = 450
graph.stops["E"].y = 475
graph.stops["F"].x = 350
graph.stops["F"].y = 475
graph.stops["G"].x = 250
graph.stops["G"].y = 475

routes_def = [
    ("Prime", (255, 0, 0), ["A", "B", "C", "D"], 2),
    ("Secundus", (0, 255, 0), ["C", "E", "F"], 2),
    ("Tertius", (0, 0, 255), ["D", "G", "F"], 2),
]
buses = dict()
v_buses = dict()
routes = dict()

random.seed(42)

with open("sim/names.pickle", "+rb") as file:
    bus_names = pickle.load(file)

tot_buses = 0

for _, _, _, buses_num in routes_def:
    tot_buses += buses_num

assert tot_buses <= len(bus_names)

random.shuffle(bus_names)
i = 0
for route_name, color, nodes, buses_num in routes_def:
    this_buses = []

    first = prev = nodes[0]
    for curr in nodes[1:]:
        graph.add_edge(prev, curr)
        prev = curr
    graph.add_edge(prev, first)

    t = 0
    for _ in range(buses_num):
        v_bus = VirtualBus(
            bus_names[i],
            route_name,
            nodes[len(nodes) // buses_num * t],
            nodes[(len(nodes) // buses_num * t + 1) % len(nodes)],
        )
        bus = TrueBus(
            bus_names[i],
            route_name,
            nodes[len(nodes) // buses_num * t],
            nodes[(len(nodes) // buses_num * t + 1) % len(nodes)],
        )
        buses[bus_names[i]] = bus
        v_buses[bus_names[i]] = v_bus
        i += 1
        t += 1

    routes[route_name] = Route(route_name, color, nodes)

# print(routes)
# print(graph)
# print(buses)

# for i in range(1000):
#     print(i)
#     for bus in buses.values():
#         bus.step()
#         print(f"{bus.name}: {bus.prev_node}->{bus.next_node}, dist={bus.distance_travelled:.2f}, waiting={bus.waiting}, fill={bus.fill}, users={bus.users}")

#     for edge in graph.edges.values():
#         edge.step()


title = "Simulation"
v_title = "Virtual buses"

img = np.ones((800, 800, 3), dtype=np.uint8) * 255

run = True

cv2.namedWindow(title)
cv2.namedWindow(v_title)

while run:
    buff_img = img.copy()

    for route in routes.values():
        start = prev = graph.stops[route.circuit[0]]

        for curr in route.circuit[1:]:
            curr = graph.stops[curr]
            cv2.line(buff_img, (prev.x, prev.y), (curr.x, curr.y), route.color, 3)
            prev = curr
        cv2.line(buff_img, (prev.x, prev.y), (start.x, start.y), route.color, 3)

    for node in graph.stops.values():
        cv2.circle(buff_img, (node.x, node.y), 5, (12,12,12), 3)

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

        col = routes[bus.route].color

        cv2.circle(buff_img, (int(pos_x), int(pos_y)), 5, col, -1)
        cv2.putText(buff_img, bus.name, (int(pos_x) + 5, int(pos_y)), fontFace=0, fontScale=.4, color=(12,12,12))

    for bus in v_buses.values():
        start = graph.stops[bus.prev_node]
        stop = graph.stops[bus.next_node]
        conn = graph.get_edge(bus.prev_node, bus.next_node)
        l = conn.length()
        dir_x = (stop.x - start.x) / l * bus.distance_travelled
        dir_y = (stop.y - start.y) / l * bus.distance_travelled
        pos_x = dir_x + start.x
        pos_y = dir_y + start.y

        col = routes[bus.route].color

        cv2.circle(v_buff_img, (int(pos_x), int(pos_y)), 5, col, -1)
        cv2.putText(v_buff_img, bus.name, (int(pos_x) + 5, int(pos_y)), fontFace=0, fontScale=.4, color=(12,12,12))

    cv2.imshow(title, buff_img)
    cv2.imshow(v_title, v_buff_img)

    key = cv2.waitKey(10)

    if key == ord("q"):
        run = False

    for bus in buses.values():
        bus.step()

    for edge in graph.edges.values():
        edge.step()

    for bus in v_buses.values():
        bus.step()

cv2.destroyAllWindows()

from dataclasses import dataclass
from typing import List, Tuple, Dict, Any
import numpy as np
import random
import pickle
import cv2
import time

MIN = 0
MAX = 1.2
WAITING_SECONDS = 60
SPEED = 200
MAX_OVERCROWD_TIME_AGO = 1024
TELEPORT = True

@dataclass
class Node:
    name: str
    x: float
    y: float


@dataclass
class Edge:
    a: Node
    b: Node

    def length(self):
        return ((self.a.x - self.b.x) ** 2 + (self.a.y - self.b.y) ** 2) ** 0.5

    def expected_steps(self):
        return self.length() / ((MAX + MIN) / 2 * SPEED)


@dataclass
class VirtualBus:
    name: str
    route: List[str]
    prev_node: str
    next_node: str
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

    def step(self, graph, v_buses, delta_t=1):
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
                next_i = 1
                for i in range(len(self.route) - 1):
                    if self.prev_node == self.route[i] and self.next_node == self.route[i+1]:
                        next_i = (i+2) % len(self.route)
                        break

                self.prev_node = self.next_node
                self.next_node = self.route[next_i]

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
            edge = Edge(self.stops[a], self.stops[b])
            self.edges[(a, b)] = edge
            return edge
        
@dataclass
class SearchBus:
    name: str
    route: List[str]
    prev_node: str
    next_node: str
    distance_travelled: float = 0.0
    waiting: float = WAITING_SECONDS
    steps_run: float = 0.0
    speed: float = (MAX + MIN) / 2 * SPEED

    def step(self, graph, other_buses):
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
                next_i = 1
                for i in range(len(self.route) - 1):
                    if self.prev_node == self.route[i] and self.next_node == self.route[i+1]:
                        next_i = (i+2) % len(self.route)
                        break

                self.prev_node = self.next_node
                self.next_node = self.route[next_i]

                for bus in other_buses.values():
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

@dataclass
class OnTravel:
    name: str
    time: str
    children: List[AtStop]

def astar(start, end, graph, vbuses):
    # Deep-copy buses
    buses = [SearchBus(bus.name, bus.route, bus.prev_node, bus.next_node, bus.distance_travelled, bus.waiting, bus.steps_run, bus.speed) for bus in vbuses]

    seconds_passed = 0
    ramifications = [None, None, start, seconds_passed, [[bus.name, bus.prev_node, bus.next_node, seconds_passed, []] for bus in buses if bus.prev_node == start and bus.waiting > 0]]
    maximum = 3600
    run = True
    while seconds_passed < maximum and run:
        for bus in buses:
            bus.step()

        seconds_passed += 1
        

# if __name__ == "__main__":
#     stops = ["A", "B", "C", "D"]
#     graph = Graph(dict(), dict())
#     graph.startup(stops)
#     graph.stops["A"].x = 2000.0
#     graph.stops["A"].y = -1000
#     graph.stops["B"].x = 1000.5
#     graph.stops["B"].y = 4000.0
#     graph.stops["C"].x = -5000
#     graph.stops["C"].y = 1000
#     graph.stops["D"].x = -2000
#     graph.stops["D"].y = 1050

#     v_buses = { "A-1": VirtualBus("A-1", "A", "A", "B"),
#             "B-1": VirtualBus("B-1", "A", "D", "A")}


#     routes = {"A":Route("A", (255, 0, 0), ["A", "B", "C", "D"])}

#     nodes = ["A", "B", "C", "D"]

#     first = prev = nodes[0]
#     for curr in nodes[1:]:
#         graph.add_edge(prev, curr)
#         prev = curr
#     graph.add_edge(prev, first)


#     v_title = "Virtual buses"

#     img = np.ones((1000, 1000, 3), dtype=np.uint8) * 255

#     run = True

#     cv2.namedWindow(v_title)

#     last_time = time.time()

#     while run:
#         buff_img = img.copy()

#         for route in routes.values():
#             start = prev = graph.stops[route.circuit[0]]

#             for curr in route.circuit[1:]:
#                 curr = graph.stops[curr]
#                 cv2.line(buff_img, (int((prev.x + 5000) / 7000 * 800 + 100), int((prev.y + 1000) / 5000 * 800 + 100)), (int((curr.x + 5000) / 7000 * 800 + 100), int((curr.y + 1000) / 5000 * 800 + 100)), route.color, 3)
#                 prev = curr
#             cv2.line(buff_img, (int((prev.x + 5000) / 7000 * 800 + 100), int((prev.y + 1000) / 5000 * 800 + 100)), (int((start.x + 5000) / 7000 * 800 + 100), int((start.y + 1000) / 5000 * 800 + 100)), route.color, 3)

#         for node in graph.stops.values():
#             cv2.circle(buff_img, (int((node.x + 5000) / 7000 * 800 + 100), int((node.y + 1000) / 5000 * 800 + 100)), 5, (12,12,12), 3)

#         v_buff_img = buff_img.copy()

#         for bus in v_buses.values():
#             start = graph.stops[bus.prev_node]
#             stop = graph.stops[bus.next_node]
#             conn = graph.get_edge(bus.prev_node, bus.next_node)
#             l = conn.length()
#             dir_x = (stop.x - start.x) / l * bus.distance_travelled
#             dir_y = (stop.y - start.y) / l * bus.distance_travelled
#             pos_x = dir_x + start.x
#             pos_y = dir_y + start.y

#             col = routes[bus.route].color

#             cv2.circle(v_buff_img, (int((pos_x + 5000) / 7000 * 800 + 100), int((pos_y + 1000) / 5000 * 800 + 100)), 5, col, -1)
#             cv2.putText(v_buff_img, bus.name, (int((pos_x + 5000) / 7000 * 800 + 100) + 10, int((pos_y + 1000) / 5000 * 800 + 100)), fontFace=0, fontScale=.4, color=(12,12,12))

#         cv2.imshow(v_title, v_buff_img)

#         key = cv2.waitKey(10)

#         if key == ord("q"):
#             run = False

#         time_now = time.time()
#         time_delta = time_now - last_time

#         last_time = time_now

#         for bus in v_buses.values():
#             bus.step(time_delta)

#     cv2.destroyAllWindows()

from dataclasses import dataclass
from typing import List, Tuple, Dict, Any, Optional
import numpy as np
import random
import pickle
import cv2
import time

MIN = 0
MAX = 1.2
WAITING_SECONDS = 60
SPEED = 190
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
                child = OnTravel(bus.name, time, bus, [], self)
                self.children.append(child)
                reached["bus-" + bus.name] = child
@dataclass
class OnTravel:
    name: str
    time: str
    bus: SearchBus
    children: List[AtStop]
    parent: AtStop

    def check(self, graph, buses, reached, time):
        for child in self.children:
            child.check(graph, buses, reached, time)

        if self.bus.waiting > 0 and reached["stop-" + self.bus.prev_node] is None:
            child = AtStop(self.bus.prev_node, time, [], self)
            self.children.append(child)
            reached["stop-" + self.bus.prev_node] = child

def directions(start, end, graph, vbuses):
    # Deep-copy buses
    buses = [SearchBus(bus.name, bus.route, bus.prev_node, bus.next_node, bus.distance_travelled, bus.waiting, bus.steps_run, bus.speed) for bus in vbuses.values()]
    
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
            bus.step(graph, buses)

        seconds_passed += 1

    if run:
        print("Couldn't find route")
    else:
        unravel = []
        elem = reached["stop-" + end]
        while elem is not None:
            unravel.append(elem)
            elem = elem.parent

        print([(elem.name, elem.time) for elem in unravel[::-1]])

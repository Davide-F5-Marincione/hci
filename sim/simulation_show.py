import random
import pickle
import cv2
import networkx as nx
import simpy
import networkx as nx
import random
import matplotlib.pyplot as plt
from dataclasses import dataclass
from typing import List, Dict, Tuple, Callable, Optional
from itertools import chain
import numpy as np


@dataclass
class BusStop:
    name: str
    waiting: int
    x: int = 100
    y: int = 100

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
    color: Tuple[int]
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

nodes = [
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
    BusStop("Augustus", 0),
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
    BusStop("Appia", 0),
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
    BusStop("Aurelia", 0)
]

nodes = {node.name: node for node in nodes}

routes = [
    Route(
        "310",
        (230, 241, 74),
        [nodes[name] for name in 
            [
                "Augustus", "Flavia","Palatina", "Caelina", "Claudia", "Quirina", "Termini", "Capitolina", "Esquilina", "Aventina", "Augustus"
            ]
        ]
    ),
    Route(
        "546",
        (87, 93, 144),
        [nodes[name] for name in
            [
                "Appia",
                "Neronia",
                "Tiberia",
                "Capitolina",
                "Aemilia",
                "Termini",
                "Horatia",
                "Valeria",
                "Pompeia",
                "Laelia",
                "Appia"
            ]
        ]   
    ),
    Route(
        "913",
        (196, 40, 71),
        [nodes[name] for name in
            [
            "Aurelia",
            "Cornelia",
            "Julia",
            "Termini",
            "Livia",
            "Octavia",
            "Poppaea",
            "Agrippina",
            "Domitia",
            "Messalina",
            "Aurelia",
            ]
        ]
    ),
]


nodes = nodes.values()

for node in nodes:
    node.x = random.randint(10, 789)
    node.y = random.randint(10, 789)

bus_graph = nx.Graph()
bus_graph.add_nodes_from(nodes)

edges = list(chain.from_iterable([route.to_edges() for route in routes]))

bus_graph.add_edges_from(edges)

title = "Interactive routes shower"

img = np.ones((800, 800, 3), dtype=np.uint8) * 255

run = True

curr_node = None
mode = 0

def distance_2d(x,y,node):
    return ((x - node.x) ** 2 + (y - node.y) ** 2) ** .5

def return_closer(x,y):
    min_d = np.inf
    started = False
    closest = None

    for node in nodes:

        d = distance_2d(x,y, node)

        if started:
            if d < min_d:
                min_d = d
                closest = node
        else:
            min_d = d
            closest = node
            started = True

    return closest, min_d

def mousePoints(event,x,y,flags,params):
    global mode
    global curr_node



    if event == cv2.EVENT_LBUTTONDOWN:
        if mode == 0:
            curr_node, d = return_closer(x,y)
            if d > 5:
                curr_node = None
            else:
                mode = 1
        elif mode == 1:
            curr_node = None
            mode = 0
    
    if mode == 1:
        curr_node.x = x
        curr_node.y = y

cv2.namedWindow(title)
cv2.setMouseCallback(title, mousePoints)

while run:
    buff_img = img.copy()

    for route in routes:
        prev = route.stops[0]
        for curr in route.stops[1:]:
            cv2.line(buff_img, (prev.x,prev.y), (curr.x,curr.y), route.color, 3)
            prev = curr

    for node in nodes:
        cv2.circle(buff_img, (node.x,node.y), 5, (0,0,0), 3)

    cv2.imshow(title, buff_img)
    
    key = cv2.waitKey(10)

    if key == ord("q"):
        run = False

cv2.destroyAllWindows()
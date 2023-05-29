from dataclasses import dataclass
import sqlite3
import random
import asyncio
import time
from datetime import datetime, timedelta
from quart import Quart, request, Response, json
import numpy as np
import cv2
import model.simulator as vsim
from dataclasses import dataclass
import re


@dataclass
class User:
    uname: str
    first_name: str
    last_name: str
    email: str

    def __post_init__(self):
        # validate email with regex
        if (
            re.match("^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\.[a-zA-Z0-9-.]+$", self.email)
            is None
        ):
            raise ValueError("Invalid Email")


app = Quart(__name__)


@app.route("/session", methods=["POST"])
async def log_in():
    r = await request.get_json()

    name = r["username"]

    cur = con.execute("SELECT auth FROM users WHERE user_name = ?", [name])
    result = cur.fetchone()

    if result is None:
        return "User not found", 404

    r = Response(
        response=json.dumps({"auth": result[0]}),
        status=200,
        mimetype="application/json",
    )
    return r


@app.route("/users", methods=["POST"])
async def registration():
    r = await request.get_json()

    try:
        user = User(r["username"], r["firstname"], r["lastname"], r["email"])
    except ValueError as e:
        return str(e), 400
    
    if r["username"] == "admin": # Shhhhhhhhhhhhhhhhhh
        return "Nope lol", 403
    
    if len(r["username"]) == 0:
        return "Try a better name", 400

    cur = con.execute("SELECT * FROM users WHERE user_name = ?", [user.uname])

    if cur.fetchone() is not None:
        return "User already exists", 409

    auth = str(random.getrandbits(64))

    while True:
        cur = con.execute("SELECT * FROM users WHERE auth = ?", [auth])
        if cur.fetchone() is None:
            break
        auth = str(random.getrandbits(64))

    cur = con.execute(
        "INSERT INTO users VALUES(:user_name, :first_name, :last_name, :email, :auth, :credits, :donations_counter, :reports_counter)",
        {
            "user_name": user.uname,
            "first_name": user.first_name,
            "last_name": user.last_name,
            "email": user.email,
            "auth": auth,
            "credits": 0,
            "donations_counter": 0,
            "reports_counter": 0,
        },
    )

    cur.close()
    con.commit()

    return "", 204


@app.route("/users/<user>/credits", methods=["GET"])
async def get_credits(user):
    headers = request.headers
    auth = headers.get("Authorization").strip("Bearer ")
    cur = con.execute(
        "SELECT user_name, credits FROM users WHERE user_name = ? AND auth = ?", [user, auth]
    )
    record = cur.fetchone()
    cur.close()
    con.commit()
    if record is None:
        return "User not found", 404

    user, curr_credits = record[0], record[1]

    return Response(
        response=json.dumps({"credits": curr_credits}),
        status=200,
        mimetype="application/json",
    )


@app.route("/users/<user>/donate", methods=["PUT"])
async def handle_users_put(user):
    headers = request.headers
    auth = headers.get("Authorization").strip("Bearer ")
    cur = con.execute(
        "SELECT user_name, credits FROM users WHERE user_name = ? AND auth = ?", [user, auth]
    )
    record = cur.fetchone()
    cur.close()
    con.commit()
    if record is None:
        return "User not found", 404

    user, curr_credits = record[0], record[1]

    r = await request.get_json()

    to_donate = r["credits"]

    if to_donate > curr_credits:
        return "Not enough credits", 400

    new_credits = curr_credits - to_donate

    cur = con.execute(
        "UPDATE users SET credits = ? WHERE user_name = ?",
        [new_credits, user],
    )
    cur.close()
    con.commit()

    cur = con.execute(
        "UPDATE users SET donations_counter = donations_counter + 1 WHERE user_name = ?",
        [user],
    )
    return "Successful operation", 200


@app.route("/users/<user>", methods=["GET"])
async def handle_users_get(user):
    headers = request.headers
    auth = headers.get("Authorization").strip("Bearer ")
    cur = con.execute(
        "SELECT first_name, last_name, email, credits, donations_counter, reports_counter FROM users WHERE user_name = ? AND auth = ?", [user, auth]
    )
    record = cur.fetchone()
    cur.close()
    con.commit()
    if record is None:
        return "User not found", 404
    

    response = {"first_name": record[0],
                "last_name": record[1],
                "email": record[2],
                "credits": record[3],
                "donations_counter": record[4],
                "reports_counter": record[5]}

    r = Response(
        response=json.dumps(response),
        status=200,
        mimetype="application/json",
    )
    return r


def bus_info(bus):
    bus = v_buses.get(bus, None)

    conn = graph.get_edge(bus.prev_node, bus.next_node)
    time_to_arrive = conn.expected_steps() - bus.steps_run + bus.waiting
    time_to_arrive = datetime.now() + timedelta(seconds=time_to_arrive)

    return {
        "line": bus.route,
        "last_seen": bus.last_signal,
        "crowdedness": bus.mu_overcrowded,
        "crowdedness_var": bus.var_overcrowded,
        "expected_from": bus.prev_node,
        "expected_to": bus.next_node,
        "calculated_delay": bus.delay,
        "expected_next_arrival": time_to_arrive,
    }


@app.route("/route", methods=["PUT"])
async def request_directions():
    data = await request.get_json()

    data_dict = {k: v for k, v in data.items()}

    ret = vsim.directions(data_dict["from"], data_dict["to"], graph, routes, v_buses)
    if ret is None:
        return Response(status=404)

    dt = datetime.now()

    res_outer = []
    for solution in ret:
        res = []
        first_stop = None
        for bus_name, stops in solution:
            bus = v_buses[bus_name]
            val = {
                "transit_type": routes[bus.route].transit_type,
                "transit_color": '#%02x%02x%02x' % routes[bus.route].color, # cool af https://stackoverflow.com/a/3380739
                "transit_name": bus_name,
                "transit_line": bus.route,
                "delay": bus.delay,
                "crowdedness_mu": bus.mu_overcrowded,
                "crowdedness_var": bus.var_overcrowded,
                "last_seen": bus.last_signal.isoformat(),
                "stops": [
                    {
                        "stop-name": name,
                        "time": (dt + timedelta(seconds=time)).isoformat(),
                    }
                    for name, time in stops
                ],
            }
            if first_stop is None:
                first_stop = val["stops"][0]["time"]
            res.append(val)
        last_stop = res[-1]["stops"][-1]["time"]
        res_outer.append({"from":data_dict["from"], "to":data_dict["to"], "departure_time": first_stop, "arrival_time":last_stop, "transits":res})

    return Response(
        response=json.dumps(res_outer), status=200, mimetype="application/json"
    )


@app.route("/buses/<bus>/signal", methods=["PUT"])
async def bus_signal(bus):
    headers = request.headers
    auth = headers.get("Authorization").strip("Bearer ")
    cur = con.execute("SELECT * FROM users WHERE auth = ?", [auth])
    a = cur.fetchone()
    cur.close()
    con.commit()
    if a is None:
        return "Not a user", 401

    data = await request.get_json()

    data_dict = {k: v for k, v in data.items()}

    if (a := v_buses.get(bus, None)) is not None:
        a.overcrowd_signal(data_dict["is_overcrowded"], buses[bus])

        cur = con.execute(
            "UPDATE users SET reports_counter = reports_counter + 1 WHERE auth = ?",
            [auth],
        )

        cur.close()

        cur = con.execute(
            "UPDATE users SET credits = credits + 3 WHERE auth = ?",
            [auth],
        )

        cur.close()
        con.commit()

        return Response(status=200)
    else:
        return Response(status=404)


async def bus_get(bus):
    bus = v_buses.get(bus, None)

    if bus is None:
        return "Bus not found", 404

    resp = bus_info(bus)

    return Response(response=json.dumps(resp), status=200, mimetype="application/json")


@app.route("/lines/<line>", methods=["GET"])
async def line_get(line):
    line = routes.get(line, None)

    if line is None:
        return "Line not found", 404

    stops = line.circuit

    buses = []

    for bus in v_buses:
        if bus.route == line.name:
            buses.append(bus_info(bus))

    resp = {"stops": stops, "buses": buses}

    return Response(response=json.dumps(resp), status=200, mimetype="application/json")


@app.route("/liveness", methods=["GET"])
async def liveness():
    return Response(
        status=200, mimetype="application/json", response=json.dumps({"status": "ok"})
    )


def shrink(x):
    return int(x / 10)


async def sim():
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
                cv2.line(
                    buff_img,
                    (shrink(prev.x), shrink(prev.y)),
                    (shrink(curr.x), shrink(curr.y)),
                    route.color[::-1],
                    3,
                )
                prev = curr
            cv2.line(
                buff_img,
                (shrink(prev.x), shrink(prev.y)),
                (shrink(start.x), shrink(start.y)),
                route.color[::-1],
                3,
            )

        for node in graph.stops.values():
            cv2.putText(
                buff_img,
                node.name,
                (shrink(node.x) + 15, shrink(node.y) + 15),
                fontFace=3,
                fontScale=0.4,
                color=(12, 12, 12),
            )
            cv2.circle(buff_img, (shrink(node.x), shrink(node.y)), 5, (12, 12, 12), 3)

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
            cv2.putText(
                buff_img,
                f"{bus.name}, {len(bus.passengers) / bus.capacity:.2f}",
                (shrink(pos_x) + 5, shrink(pos_y)),
                fontFace=0,
                fontScale=0.4,
                color=(12, 12, 12),
            )

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
            cv2.putText(
                v_buff_img,
                f"{bus.name}, {bus.mu_overcrowded:.2f}~{bus.var_overcrowded**.5:.2f}",
                (shrink(pos_x) + 5, shrink(pos_y)),
                fontFace=0,
                fontScale=0.4,
                color=(12, 12, 12),
            )

        cv2.imshow(title, buff_img)
        cv2.imshow(v_title, v_buff_img)

        key = cv2.waitKey(5)

        if key == ord("q"):
            run = False

        time_now = time.time()
        time_delta = time_now - last_time

        last_time = time_now

        for bus in buses.values():
            bus.step(graph, routes, v_buses, time_delta)

        for edge in graph.edges.values():
            edge.step()

        await asyncio.sleep(5e-3)

    cv2.destroyAllWindows()


async def main():
    await asyncio.gather(sim(), app.run_task(host="0.0.0.0"))


if __name__ == "__main__":
    stops = ["A", "B", "C", "D", "E", "F", "G"]
    graph = vsim.Graph(dict(), dict())
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

    v_buses = {
        "A1": vsim.VirtualBus("A1", "A", "A", "B"),
        "A3": vsim.VirtualBus("A3", "A", "C", "D"),
        "A2": vsim.VirtualBus("A2", "A", "D", "C", -1),
        "B1": vsim.VirtualBus("B1", "B", "C", "E"),
        "B2": vsim.VirtualBus("B2", "B", "F", "E", -1),
        "C1": vsim.VirtualBus("C1", "C", "D", "G"),
        "C2": vsim.VirtualBus("C2", "C", "F", "G", -1),
    }

    buses = {
        "A1": vsim.TrueBus("A1", "A", "A", "B"),
        "A3": vsim.TrueBus("A3", "A", "C", "D"),
        "A2": vsim.TrueBus("A2", "A", "D", "C", -1),
        "B1": vsim.TrueBus("B1", "B", "C", "E"),
        "B2": vsim.TrueBus("B2", "B", "F", "E", -1),
        "C1": vsim.TrueBus("C1", "C", "D", "G"),
        "C2": vsim.TrueBus("C2", "C", "F", "G", -1),
    }

    routes = {
        "A": vsim.Route("A", (205, 92, 92), ["A", "B", "C", "D"], "tram"),
        "B": vsim.Route("B", (0, 49, 83), ["C", "E", "F"], "bus"),
        "C": vsim.Route("C", (1,68,33), ["D", "G", "F"], "bus"),
    }

    for route in routes.values():
        this_buses = []

        first = prev = route.circuit[0]
        for curr in route.circuit[1:]:
            graph.add_edge(prev, curr)
            prev = curr
        graph.add_edge(prev, first)

    con = sqlite3.connect("tutorial.db")
    try:
        with open("sim/schema.sql") as f:
            con.execute(f.read())
            con.commit()
    except:
        print(
            "Schema not found, perhaps you are not running from the root directory? (hci/)"
        )

    asyncio.run(main())

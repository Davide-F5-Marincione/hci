from dataclasses import dataclass
import sqlite3
import random
from quart import Quart, request, Response, json
import asyncio
import model.virtual_sim as vsim
import time
import numpy as np
import cv2
from datetime import datetime, timedelta

app = Quart(__name__)

@app.route("/users", methods=["POST"])
async def create_user():
    if request.method == "POST":
        r = request.get_json()

        name = r["name"]

        cur = con.execute("SELECT * FROM users WHERE name = ?", [name])
        result = cur.fetchone()

        status = 200

        if result is None:
            auth = 0
            is_free = False
            while not is_free:
                auth = random.getrandbits(64)
                cur = con.execute("SELECT * FROM users WHERE auth = ?", [int(auth)])
                is_free = cur.fetchone() is None
            cur = con.execute("INSERT INTO users VALUES(:name, :auth, :credits)", {"name":name, "auth": auth, "credits":0})
            cur.close()
            con.commit()
            status = 201
        else:
            auth = result[1]
        r = Response(response=json.dumps({"auth": auth}), status=status, mimetype="application/json")
        return r
    return "Internal Server Error", 500


@app.route("/users/<user>", methods=["GET", "PUT"])
async def handle_users(user):
    headers = request.headers
    auth = headers.get('Authorization')
    cur = con.execute("SELECT * FROM users WHERE name = ? AND auth = ?", [user, auth])
    a = cur.fetchone()
    cur.close()
    con.commit()
    if a is None:
        return "User not found", 404

    user, _, credits = a

    if request.method == "GET":
        r = Response(response=json.dumps({"credits": credits}), status=200, mimetype="application/json")
        return r
    elif request.method == "PUT":
        r = request.get_json()
        cur = con.execute("UPDATE users SET credits = credits - ? WHERE name = ?", [r["credits"], user])
        cur.close()
        con.commit()
        return "Successful operation", 204
    return "Internal Server Error", 500

@app.route("/route", methods=["put"])
async def request_directions():

    data = await request.get_json()

    data_dict = {k: v for k, v in data.items()}

    ret = vsim.directions(data_dict["from"], data_dict["to"], graph, routes, v_buses)
    if ret is None:
        return Response(status=404)
    
    dt = datetime.now()
    
    res = []
    for bus_name, stops in ret:
        val = {"bus": bus_name, "stops": [{"stop-name": name, "time": (dt + timedelta(seconds=time)).isoformat()} for name, time in stops]}
        res.append(val)

    return Response(response=json.dumps(res), status=200, mimetype="application/json")
    
@app.route("/buses/<bus>", methods=["put"])
async def bus_put_data(bus):

    data = await request.get_json()

    data_dict = {k: v for k, v in data.items()}

    if data_dict.get("overcrowded", None) is not None:
        
        if (a:=v_buses.get(bus, None)) is not None:
            a.overcrowd()
            return Response(status=200)
        else:
            return Response(status=404)

    elif data_dict.get("boardedat", None) is not None:

        if (a:=v_buses.get(bus, None)) is not None:
            a.board_signal(data_dict["from"], data_dict["to"], graph, data_dict["boardedat"])
            return Response(status=200)
        else:
            return Response(status=404)

    else:
        print(f"invalid request")
        # 400 Bad Request
        return Response(status=400)
    
def shrink(x):
    return int(x/10)

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
                cv2.line(buff_img, (shrink(prev.x), shrink(prev.y)), (shrink(curr.x), shrink(curr.y)), route.color, 3)
                prev = curr
            cv2.line(buff_img, (shrink(prev.x), shrink(prev.y)), (shrink(start.x), shrink(start.y)), route.color, 3)

        for node in graph.stops.values():
            cv2.putText(buff_img, node.name, (shrink(node.x) + 15, shrink(node.y) + 15), fontFace=3, fontScale=.4, color=(12,12,12))
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

        key = cv2.waitKey(5)

        if key == ord("q"):
            run = False

        time_now = time.time()
        time_delta = time_now - last_time

        last_time = time_now

        for bus in buses.values():
            bus.step(graph, routes, buses, v_buses, time_delta)

        for edge in graph.edges.values():
            edge.step()

        await asyncio.sleep(5e-3)

    cv2.destroyAllWindows()

async def main():
    await asyncio.gather(
        sim(),
        app.run_task()
    )

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

    v_buses = { "A1": vsim.VirtualBus("A1", "A", "A", "B"),
                "A3": vsim.VirtualBus("A3", "A", "C", "D"),
                "A2": vsim.VirtualBus("A2", "A", "D", "C", -1),
                "B1": vsim.VirtualBus("B1", "B", "C", "E"),
                "B2": vsim.VirtualBus("B2", "B", "F", "E", -1),
                "C1": vsim.VirtualBus("C1", "C", "D", "G"),
                "C2": vsim.VirtualBus("C2", "C", "F", "G", -1)}

    buses = {   "A1": vsim.TrueBus("A1", "A", "A", "B"),
                "A3": vsim.TrueBus("A3", "A", "C", "D"),
                "A2": vsim.TrueBus("A2", "A", "D", "C", -1),
                "B1": vsim.TrueBus("B1", "B", "C", "E"),
                "B2": vsim.TrueBus("B2", "B", "F", "E", -1),
                "C1": vsim.TrueBus("C1", "C", "D", "G"),
                "C2": vsim.TrueBus("C2", "C", "F", "G", -1)}

    routes = {"A":vsim.Route("A", (255, 0, 0), ["A", "B", "C", "D"]),
                "B":vsim.Route("B", (0, 255, 0), ["C", "E", "F"]),
                "C":vsim.Route("C", (0, 0, 255), ["D", "G", "F"])}
    
    for route in routes.values():
        this_buses = []

        first = prev = route.circuit[0]
        for curr in route.circuit[1:]:
            graph.add_edge(prev, curr)
            prev = curr
        graph.add_edge(prev, first)


    con = sqlite3.connect("tutorial.db")
    con.execute("CREATE TABLE IF NOT EXISTS users(name TEXT PRIMARY KEY, auth INTEGER UNIQUE NOT NULL, credits INTEGER NOT NULL CHECK (credits >= 0) DEFAULT 0)")
    #con.execute("CREATE TABLE IF NOT EXISTS buses(name TEXT PRIMARY KEY, last_seen TEXT NOT NULL, time_ago DATETIME NOT NULL, should_be TEXT NOT NULL, distance INTEGER NOT NULL)")
    con.commit()


    asyncio.run(main())
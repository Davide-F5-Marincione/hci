from dataclasses import dataclass
import sqlite3
import random
from quart import Quart, request, Response, json
import asyncio
import model.virtual_sim as vsim
import time
import numpy as np
import cv2

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
        print(data_dict["boardedat"])
        print(data_dict["from"])
        print(data_dict["to"])

        if (a:=v_buses.get(bus, None)) is not None:
            a.board_signal(data_dict["from"], data_dict["to"], graph, data_dict["boardedat"])
            return Response(status=200)
        else:
            return Response(status=404)

    else:
        print(f"invalid request")
        # 400 Bad Request
        return Response(status=400)

async def sim():
    v_title = "Virtual buses"

    img = np.ones((1000, 1000, 3), dtype=np.uint8) * 255

    run = True

    cv2.namedWindow(v_title)

    last_time = time.time()

    while run:
        await asyncio.sleep(5e-3)
        buff_img = img.copy()

        for route in routes.values():
            start = prev = graph.stops[route.circuit[0]]

            for curr in route.circuit[1:]:
                curr = graph.stops[curr]
                cv2.line(buff_img, (int((prev.x + 5000) / 7000 * 800 + 100), int((prev.y + 1000) / 5000 * 800 + 100)), (int((curr.x + 5000) / 7000 * 800 + 100), int((curr.y + 1000) / 5000 * 800 + 100)), route.color, 3)
                prev = curr
            cv2.line(buff_img, (int((prev.x + 5000) / 7000 * 800 + 100), int((prev.y + 1000) / 5000 * 800 + 100)), (int((start.x + 5000) / 7000 * 800 + 100), int((start.y + 1000) / 5000 * 800 + 100)), route.color, 3)

        for node in graph.stops.values():
            cv2.putText(buff_img, node.name, (int((node.x + 5000) / 7000 * 800 + 100) + 10, int((node.y + 1000) / 5000 * 800 + 100)), fontFace=0, fontScale=.4, color=(12,12,12))
            cv2.circle(buff_img, (int((node.x + 5000) / 7000 * 800 + 100), int((node.y + 1000) / 5000 * 800 + 100)), 5, (12,12,12), 3)

        v_buff_img = buff_img.copy()

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

            cv2.circle(v_buff_img, (int((pos_x + 5000) / 7000 * 800 + 100), int((pos_y + 1000) / 5000 * 800 + 100)), 5, col, -1)
            cv2.putText(v_buff_img, bus.name, (int((pos_x + 5000) / 7000 * 800 + 100) + 10, int((pos_y + 1000) / 5000 * 800 + 100)), fontFace=0, fontScale=.4, color=(12,12,12))

        cv2.imshow(v_title, v_buff_img)

        key = cv2.waitKey(5)

        if key == ord("q"):
            run = False

        time_now = time.time()
        time_delta = time_now - last_time

        last_time = time_now

        for bus in v_buses.values():
            bus.step(graph, v_buses, time_delta)
        

    cv2.destroyAllWindows()

async def main():
    await asyncio.gather(
        sim(),
        app.run_task()
    )

if __name__ == "__main__":


    stops = ["A", "B", "C", "D"]
    graph = vsim.Graph(dict(), dict())
    graph.startup(stops)
    graph.stops["A"].x = 2000.0
    graph.stops["A"].y = -1000
    graph.stops["B"].x = 1000.5
    graph.stops["B"].y = 4000.0
    graph.stops["C"].x = -5000
    graph.stops["C"].y = 1000
    graph.stops["D"].x = -2000
    graph.stops["D"].y = 1050

    v_buses = { "A-1": vsim.VirtualBus("A-1", ["A", "B", "C", "D", "C", "B"], "A", "B"),
            "B-1": vsim.VirtualBus("B-1", ["D", "C", "B", "A", "B", "C"], "D", "C")}

    routes = {"A":vsim.Route("A", (255, 0, 0), ["A", "B", "C", "D"])}

    nodes = ["A", "B", "C", "D"]

    first = prev = nodes[0]
    for curr in nodes[1:]:
        graph.add_edge(prev, curr)
        prev = curr
    graph.add_edge(prev, first)

    vsim.directions("B", "D", graph, v_buses)


    con = sqlite3.connect("tutorial.db")
    con.execute("CREATE TABLE IF NOT EXISTS users(name TEXT PRIMARY KEY, auth INTEGER UNIQUE NOT NULL, credits INTEGER NOT NULL CHECK (credits >= 0) DEFAULT 0)")
    #con.execute("CREATE TABLE IF NOT EXISTS buses(name TEXT PRIMARY KEY, last_seen TEXT NOT NULL, time_ago DATETIME NOT NULL, should_be TEXT NOT NULL, distance INTEGER NOT NULL)")
    con.commit()


    asyncio.run(main())
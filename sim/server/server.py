from flask import Flask
from flask import request
from flask import Response
from flask import json
from dataclasses import dataclass
import sqlite3
import random

app = Flask(__name__)

@app.route("/users", methods=["POST"])
def create_user():
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
def handle_users(user):
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


if __name__ == "__main__":
    con = sqlite3.connect("tutorial.db")
    con.execute("CREATE TABLE IF NOT EXISTS users(name TEXT PRIMARY KEY, auth INTEGER UNIQUE NOT NULL, credits INTEGER NOT NULL CHECK (credits >= 0) DEFAULT 0)")
    #con.execute("CREATE TABLE IF NOT EXISTS buses(name TEXT PRIMARY KEY, last_seen TEXT NOT NULL, time_ago DATETIME NOT NULL, should_be TEXT NOT NULL, distance INTEGER NOT NULL)")
    con.commit()
    app.run(debug=False, threaded=False)

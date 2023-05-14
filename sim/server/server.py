from flask import Flask
from flask import request
from flask import Response
from flask import json
from dataclasses import dataclass
import sqlite3

app = Flask(__name__)

@app.route("/users", methods=["POST"])
def create_user():
    if request.method == "POST":
        r = request.get_json()
        cur = con.execute("INSERT INTO users VALUES(:name, :credits)", {"name":r["name"], "credits":0})
        cur.close()
        con.commit()
        return "Successful operation", 204
    return "Internal Server Error", 500


@app.route("/users/<user>", methods=["GET", "PUT"])
def handle_users(user):
    cur = con.execute("SELECT * FROM users WHERE name = ?", [user])
    a = cur.fetchone()
    cur.close()
    con.commit()
    if a is None:
        return "User not found", 404
    
    user, credits = a

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
    con.execute("CREATE TABLE IF NOT EXISTS users(name TEXT PRIMARY KEY, credits INTEGER NOT NULL CHECK (credits >= 0) DEFAULT 0)")
    con.commit()
    app.run(debug=False, threaded=False)

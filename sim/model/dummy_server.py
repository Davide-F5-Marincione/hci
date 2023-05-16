from flask import Flask, request, jsonify, Response
import asyncio

app = Flask(__name__)

@app.route("/buses/<bus>", methods=["POST"])
def bus_put_data(bus):

    data = request.get_json()

    # turn to dict

    data_dict = {k: v for k, v in data.items()}

    if data_dict["overcrowded"]:

        print(f"Bus {bus} is overcrowded!")
        return Response(status=200)

    elif data_dict["boarded"]:

        print(f"A passenger has boarded at {data_dict['boardedat']}")
        return Response(status=200)

    else:
        print(f"invalid request")
        # 400 Bad Request
        return Response(status=400)

if __name__ == "__main__":
    app.run(debug=True)

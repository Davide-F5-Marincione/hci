from quart import Quart
import asyncio

app = Quart(__name__)

@app.route('/')
async def hello_api():
    return 'hello'


async def hello():
    while True:

        await asyncio.sleep(1)
        print("hello")

async def main():

    await asyncio.gather(
        hello(),
        app.run_task()
    )

if __name__ == "__main__":
    asyncio.run(main())

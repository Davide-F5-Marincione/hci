import random
import pickle


stops = [
    "Castro Laurenziano",
    "Viale Ippocrate",
    "Piazzale Aldo Moro",
    "Policlinico",
    "Verano",
    "Piazzale delle Provincie",
    "Tiburtina",
    "Termini",
    "Porta Pia",
    "Castro Pretorio",
    "Piazza dei Sanniti",
]

lines = {
    "1": (
        3,
        [
            ("Castro Laurenziano", 60),
            ("Viale Ippocrate", 60),
            ("Policlinico", 120),
            ("Piazzale Aldo Moro", 240),
            ("Piazza dei Sanniti", 80),
            ("Verano", 60),
        ],
    )
}

delays = [0, 10, 30, 60, 120, 240]
weights = [10, 20, 30, 40, 50, 60]


def step(state):
    for busname, (this_line, last_place, time_left, overcrowded) in state.items():
        next_place = last_place
        time_left -= 10
        if time_left <= 0:
            line = lines[this_line][1]
            for i, (place, _) in enumerate(line):
                if place == last_place:
                    next_place, time_left = line[(i + 1) % len(line)]

                    time_left += random.choices(delays, weights, k=1)[0]
                    overcrowded = random.choices([True, False], [100, 50], k=1)[0]
                    break

        state[busname] = (this_line, next_place, time_left, overcrowded)


def random_setup(lines, names="sim/names.pickle", seed=None):
    if seed is not None:
        random.seed(seed)

    with open(names, "+rb") as file:
        bus_names = pickle.load(file)

    n = 0

    for v, _ in lines.values():
        n += v

    assert n <= len(bus_names)

    random.shuffle(bus_names)

    i = 0
    buses = dict()

    for line, (n, list) in lines.items():
        assert n < len(list)

        pos = [elem for elem in list]
        random.shuffle(pos)

        for next_place, time_left in pos[:n]:
            time_left += random.choices(delays, weights, k=1)[0]
            overcrowded = random.choices([True, False], [100, 50], k=1)[0]
            buses[bus_names[i]] = (line, next_place, time_left, overcrowded)
            i += 1

    return buses


if __name__ == "__main__":
    buses = random_setup(lines, seed=42)

    for t in range(0, 1000, 10):
        step(buses)
        print(buses)

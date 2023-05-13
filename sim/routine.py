import random

stops = ["Castro Laurenziano", "Viale Ippocrate",
         "Piazzale Aldo Moro", "Policlinico",
         "Verano", "Piazzale delle Provincie",
         "Tiburtina", "Termini", "Porta Pia", "Castro Pretorio",
         "Piazza dei Sanniti"]

lines = {
    "1": [("Castro Laurenziano", 60), ("Viale Ippocrate", 60), ("Policlinico", 120), ("Piazzale Aldo Moro", 240), ("Piazza dei Sanniti", 80), ("Verano", 60)]
}

delays = [0, 10, 30, 60, 120, 240]
weights = [10, 20, 30, 40, 50, 60]

def step(state):
    new_state = []
    for this_line, last_place, time_left, overcrowded in state:
        next_place = last_place
        time_left -= 10
        if time_left <= 0:
            line = lines[this_line]
            for i, (place, _) in enumerate(line):
                if place == last_place:
                    next_place, time_left = line[(i+1) % len(line)]

                    time_left += random.choices(delays, weights, k=1)[0]
                    overcrowded = random.choices([True, False], [100, 50], k=1)[0]
                    break
        
        new_state.append((this_line, next_place, time_left, overcrowded))

    return new_state


curr_state = [("1", "Castro Laurenziano", 0, True), ("1", "Piazzale Aldo Moro", 0, False)]

for t in range(0, 1000, 10):
    curr_state = step(curr_state)
    print(curr_state)
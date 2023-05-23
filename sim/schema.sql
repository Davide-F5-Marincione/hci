CREATE TABLE IF NOT EXISTS "users" (
	"user_name"	TEXT NOT NULL,
	"first_name"	INTEGER NOT NULL,
	"last_name"	INTEGER NOT NULL,
	"email"	TEXT NOT NULL,
	"auth"	INTEGER NOT NULL UNIQUE,
	"credits"	INTEGER NOT NULL DEFAULT 0 CHECK("credits" >= 0),
	"donations_counter"	INTEGER NOT NULL DEFAULT 0 CHECK("donations_counter" >= 0),
	"reports_counter"	INTEGER NOT NULL DEFAULT 0 CHECK("reports_counter" >= 0),
	PRIMARY KEY("user_name")
);

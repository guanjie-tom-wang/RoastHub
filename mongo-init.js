
db = db.getSiblingDB('roasthub');

db.createCollection("toronto");
db.createCollection("waterloo");
db.createCollection("mississauga");
db.createCollection("kitchener");

db.breakfast.files.createIndex({"uploadDate": 1}, { unique: true });
db.lunch.files.createIndex({"uploadDate": 1}, { unique: true });
db.dinner.files.createIndex({"uploadDate": 1}, { unique: true });
db.snack.files.createIndex({"uploadDate": 1}, { unique: true });

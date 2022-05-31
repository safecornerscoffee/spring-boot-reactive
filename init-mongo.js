db.createUser(
    {
        user: "banana",
        pwd: "banana",
        roles: [
            {
                role: "readWrite",
                db: "banana"
            }
        ]
    }
)
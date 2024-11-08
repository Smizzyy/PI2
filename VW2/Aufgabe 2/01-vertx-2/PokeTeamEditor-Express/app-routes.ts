const express = require('express');
const app = express();
const bodyParser = require('body-parser');
app.use(bodyParser.json());

const path = require('path');
app.use(express.static(path.join(__dirname, 'public')));

// Im Browser: http://127.0.0.1:3000/roll-a-d6
// Test: curl -X GET http://localhost:3000/roll-a-d6
app.get('/roll-a-d6', (req, res) => {
    const randomNumber = Math.floor(Math.random() * 6) + 1;
    res.send(`Rolled a d6: ${randomNumber}`);
});

// Test: curl -X POST -H "Content-Type: application/json" -d '{"n":3, "d":6}' http://localhost:3000/roll
app.post("/roll", (req: Request, res: Response) => {
    console.log(req.body);
    let result = 0;
    for (let i = 0; i < req.body.n; i++) {
        result += Math.floor(Math.random() * req.body.d) + 1;
    }
    res.status(200).send(JSON.stringify({sum:result}));
});

const port:number = 3000;
app.listen(port, () => {
    console.log(`Server läuft auf Port ${port}`);
});

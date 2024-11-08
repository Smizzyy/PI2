import {Pokemon} from "./Pokemon";

const express = require('express');
const app = express();
const bodyParser = require('body-parser');
app.use(bodyParser.urlencoded({ extended: true }));

// Data to store pokemons
let pokemons: Pokemon[] = [];

// Static file handler (used for images)
const path = require('path');
app.use(express.static(path.join(__dirname, 'public')));

// Dynamic handlers
// NOTE: Usually HTML-responses are not created through string manipulation. Instead, you should use a template engine.
//       However, since we did not cover template engines yet, we do it the simple string way.
const HEADER: string = `
    <!DOCTYPE html>
    <html lang="en">
    <head>
        <meta charset="UTF-8">
        <title>Poke-Team Editor</title>
        <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/@exampledev/new.css@1.1.2/new.min.css">
    </head>
    <body>    
        <header>
            <h1>Poke-Team Editor</h1>
            <nav><a href="/">Pokemon hinzufügen</a> – <a href="/list">Alle Pokemon anzeigen</a> – <a href="/api/list">JSON API</a></nav>
        </header>
`;
const FOOTER: string = `</body></html>`;

app.get('/', (req, res) => {
    res.send(`
        ${HEADER}    
        <h2>Neues Pokemon hinzufügen</h2>
        <form action="/pokemon" method="post">
            <label for="name">Name:</label><br>
            <input type="text" id="name" name="name" required><br><br>
            <label for="pokedexNummer">Pokedex Nummer:</label><br>
            <input type="number" id="pokedexNummer" name="pokedexNummer" min="1" max="151" required><br><br>
            <label for="level">Level:</label><br>
            <input type="number" id="level" name="level" min="1" max="99" required><br><br>
            <label for="status">Status:</label><br>
            <select id="status" name="status" required>
                <option value="Healthy">Healthy</option>
                <option value="Burned">Burned</option>
                <option value="Frozen">Frozen</option>
                <option value="Paralyzed">Paralyzed</option>
                <option value="Poisoned">Poisoned</option>
                <option value="Asleep">Asleep</option>
            </select><br><br>
            <button type="submit">Speichern</button>
        </form>
        ${FOOTER}
    `);
})

app.post('/pokemon', (req, res) => {
    // Read submitted form data and add pokemon to list
    // Note: Usually this should also validate if the Pokemon is valid
    //       For example, one could submit a pokemon with an invalid level of 9000 through curl.
    const { name, pokedexNummer, level, status } = req.body;
    const newPokemon = new Pokemon(name, parseInt(pokedexNummer), parseInt(level), status);
    pokemons.push(newPokemon);

    res.send(`
        ${HEADER}
        <h2>Pokemon gespeichert</h2>
        <img src="${newPokemon.getImageURL()}">
        <p><strong>Name:</strong> ${name}</p>
        <p><strong>Pokedex Nummer:</strong> ${pokedexNummer}</p>
        <p><strong>Level:</strong> ${level}</p>
        <p><strong>Status:</strong> ${status}</p>
        <a href="/">Zurück</a>
        ${FOOTER}
  `);
});

app.get('/list', (req, res) => {
    let tableRows = pokemons.map(pokemon => `
        <tr>
          <td><img width="100px" src="${pokemon.getImageURL()}"></td>
          <td>${pokemon.name}</td>
          <td>${pokemon.pokedexNumber}</td>
          <td>${pokemon.level}</td>
          <td>${pokemon.status}</td>
        </tr>
      `).join('');

    res.send(`
        ${HEADER}
        <h2>Alle gespeicherten Pokemon</h2>
        <table border="1">
          <tr>
            <th>Image</th>
            <th>Name</th>
            <th>Pokedex Nummer</th>
            <th>Level</th>
            <th>Status</th>
          </tr>
          ${tableRows}
        </table>
        ${FOOTER}
    `);
});

app.get('/api/list', (req, res) => {
    res.json(pokemons);
})


const port:number = 3000;
app.listen(port, () => {
    console.log(`Server läuft auf Port ${port}`);
});

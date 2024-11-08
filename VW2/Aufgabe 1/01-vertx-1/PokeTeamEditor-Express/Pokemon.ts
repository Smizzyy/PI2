export enum PokemonStatus {
    Healthy = "Healthy",
    Burned = "Burned",
    Frozen = "Frozen",
    Paralyzed = "Paralyzed",
    Poisoned = "Poisoned",
    Asleep = "Asleep"
}

export class Pokemon {
    name: string;
    pokedexNumber: number;
    level: number;
    status: PokemonStatus;

    constructor(name, pokedexNumber, level, status) {
        this.name = name;
        this.pokedexNumber = pokedexNumber;
        this.level = level;
        this.status = status;
    }

    getImageURL(): string {
        let textid = this.pokedexNumber.toString();
        while (textid.length < 3) {
            textid = '0' + textid;
        }
        return `./images/pokemon/${textid}.png`;
    }
}

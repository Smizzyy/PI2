# Aufgabe 1

- **Grafiktreiber**:  
  Der Grafiktreiber ist in **Schicht 2** einzuordnen, weil der Treiber die Verbindung von Anwendungen zur Grafikkarte über die Hardwareschnittstelle herstellt.

- **Fortnite**:  
  Fortnite ist in **Schicht 4**, da es sich um eine Anwendung handelt. Der Benutzer kann dann über die Benutzerschnittstelle mit der Anwendung interagieren.

- **Windows Defender**:  
  Windows Defender ist in **Schicht 3**, weil es ein vorinstallierter Systemdienst ist, der auf jedem Windows-Rechner zu finden ist. Es überprüft z. B. über die hardwareabhängige Schnittstelle, ob der Systemkern von Schadsoftware befallen sein könnte.

- **Shell**:  
  Die Shell ist in **Schicht 4** einzuordnen, da sie dem Benutzer über die Benutzerschnittstelle erlaubt, mit dem System zu interagieren.

- **FACEIT Anti-Cheat**:  
  FACEIT Anti-Cheat ist in **Schicht 2**, da es mit einem Treiber auf Kernebene arbeitet, aber auch in **Schicht 4**, da es eine Anwendung aufruft, wenn ein von FACEIT geschütztes Spiel gestartet wird.  
  - Es prüft über die Systemschnittstelle, ob andere Anwendungen laufen, die das Cheaten ermöglichen.  
  - Die Anwendung prüft ebenfalls über die Benutzerschnittstelle die Eingaben des Benutzers.


# Aufgabe 2.2

## mit Zeitscheibe 100ms:

| **Schritt** | **Prozess** | **Laufzeit (ms)** | **Verbleibende Zeit (ms)** | **Status**          |
|-------------|-------------|-------------------|----------------------------|---------------------|
| 1           | A           | 100               | 300                        | Läuft               |
| 2           | B           | 100               | 100                        | Läuft               |
| 3           | C           | 100               | 700                        | Läuft               |
| 4           | D           | 100               | 500                        | Läuft               |
| 5           | E           | 100               | 900                        | Läuft               |
| 6           | A           | 100               | 200                        | Läuft               |
| 7           | B           | 100               | -                          | Abgeschlossen (700) |
| 8           | C           | 100               | 600                        | Läuft               |
| 9           | D           | 100               | 400                        | Läuft               |
| 10          | E           | 100               | 800                        | Läuft               |
| 11          | A           | 100               | 100                        | Läuft               |
| 12          | C           | 100               | 500                        | Läuft               |
| 13          | D           | 100               | 300                        | Läuft               |
| 14          | E           | 100               | 700                        | Läuft               |
| 15          | A           | 100               | -                          | Abgeschlossen (1500)|
| 16          | C           | 100               | 400                        | Läuft               |
| 17          | D           | 100               | 200                        | Läuft               |
| 18          | E           | 100               | 600                        | Läuft               |
| 19          | C           | 100               | 300                        | Läuft               |
| 20          | D           | 100               | 100                        | Läuft               |
| 21          | E           | 100               | 500                        | Läuft               |
| 22          | C           | 100               | 200                        | Läuft               |
| 23          | D           | 100               | -                          | Abgeschlossen (2300)|
| 24          | E           | 100               | 400                        | Läuft               |
| 25          | C           | 100               | 100                        | Läuft               |
| 26          | E           | 100               | 300                        | Läuft               |
| 27          | C           | 100               | -                          | Abgeschlossen (2700)|
| 28          | E           | 100               | 200                        | Läuft               |
| 29          | E           | 100               | 100                        | Läuft               |
| 30          | E           | 100               | -                          | Abgeschlossen (3000)|

## Zusammenfassung der Abschlusszeiten:
**B**: 700 ms <br>
**A**: 1500 ms <br>
**D**: 2300 ms <br>
**C**: 2700 ms <br>
**E**: 3000 ms <br>

## Nach Änderung der Zeitscheibe auf 200ms:

# Ablaufplan

| **Schritt** | **Prozess** | **Laufzeit (ms)** | **Verbleibende Zeit (ms)** | **Status**          |
|-------------|-------------|-------------------|----------------------------|---------------------|
| 1           | A           | 200               | 200                        | Läuft               |
| 2           | B           | 200               | -                          | Abgeschlossen (400) |
| 3           | C           | 200               | 600                        | Läuft               |
| 4           | D           | 200               | 400                        | Läuft               |
| 5           | E           | 200               | 800                        | Läuft               |
| 6           | A           | 200               | -                          | Abgeschlossen (1200)|
| 7           | C           | 200               | 400                        | Läuft               |
| 8           | D           | 200               | 200                        | Läuft               |
| 9           | E           | 200               | 600                        | Läuft               |
| 10          | C           | 200               | 200                        | Läuft               |
| 11          | D           | 200               | -                          | Abgeschlossen (2200)|
| 12          | E           | 200               | 400                        | Läuft               |
| 13          | C           | 200               | -                          | Abgeschlossen (2600)|
| 14          | E           | 200               | 200                        | Läuft               |
| 15          | E           | 200               | -                          | Abgeschlossen (3000)|

## Zusammenfassung der Abschlusszeiten:
**B**: 400 ms <br>
**A**: 1200 ms <br>
**D**: 2200 ms <br>
**C**: 2600 ms <br>
**E**: 3000 ms <br>

## Unterschied:
Die Abschlusszeiten der einzelnen Prozesse verringert sich, wenn man die Zeitscheibe vergrößert. Im Umkehrschluss wird die Abschlusszeit der Prozesse größer, wenn man die Zeitscheibe verringert. Dies trifft allerdings nicht auf den längsten Prozess zu. Dieser hat unabhängig von der Zeitscheibe immer die gleiche Laufzeit. Auch die Gesamtlaufzeit der Prozesse wird durch das Verändern der Zeitscheibe nicht beeinflusst.

# Aufgabe 2.3
1. **Fairness**: Der Algorithmus ist fair, weil immer nur ein Prozess bearbeitet wird und diesem somit alle Ressourcen zur Verfügung stehen. Außerdem wird jeder Prozess in jedem Schritt gleich lang bearbeitet.
2. **Effizienz**: Der Algorithmus arbeitet effizient, da es zwischen dem Prozesswechsel keinen Leerlauf gibt. Nach Abschließen eines Prozesses wird dieser aus der Queue entfernt, sodass die Queue mit jedem Abschluss eines Prozesses kleiner wird.
3. **Deadlock-Vermeidung**: Da jeder Prozess nur für eine bestimmte Zeit bearbeitet wird, entsteht kein Deadlock. 
4. **Durchsatz**: Der Durchsatz ist abhängig von der Anzahl der Prozesse und die Größe der Zeitscheibe. Je größer die Zeitscheibe und geringer die Anzahl der Prozesse, desto besser ist der Durchsatz für den Algorithmus, allerdings wird der Durchsatz mit wachsender Anzahl an Prozessen und/oder verkleinern der Zeitscheibe immer schlechter.
5. **Antwortzeit**: Weil jeder Prozess innerhalb eines festen Zeitintervalls bearbeitet wird, hat der Algorithmus eine gute Antwortzeit.

# Aufgabe 3

**Analyse Deadlock**: Die beiden Tasks blockieren sich gegenseitig, weil beide parallel ausgeführt werden und die Tasks auf die jeweilige Ressource zugreifen, die gerade vom anderen Task verwendet wird. Daher kommt es zum Deadlock. **Task1** sperrt **Ressource A** und will dann **Ressource B** sperren, die aber bereits von **Task2** gesperrt wurde, welcher wiederum **Ressource A** sperren will, die durch **Task1** gesperrt ist.  

**Lösung Deadlock**: In **Task2** die selbe Reihenfolge für den Zugriff auf die Ressourcen verwendet wie in **Task1**. 

**Analyse RaceCondition**: Die RaceCondition entsteht, weil thread3 und thread4 gleichzeitig auf den sharedCounter zugreifen und diesen erhöhen. Außerdem fügen beide zeitgleich die Werte zur Liste hinzu, was zu einer fehlerhaften Reihenfolge führt.

**Lösung RaceCondition**: sharedCounter und die Liste müssen so geschützt werden, dass immer nur ein Thread auf die jeweilige Ressource zugreifen kann. So wird der Counter korrekt erhöht und die Reihenfolge in der Liste stimmt.

Die Erklärungen zu den Veränderungen im Code befinden sich im Code in den Kommentaren an den betreffenden Stellen.
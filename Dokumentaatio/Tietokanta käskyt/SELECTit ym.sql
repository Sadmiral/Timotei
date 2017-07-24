SELECT osoite1, latitude, longitude FROM reitti JOIN paketti ON " + pktID + " = reittiID JOIN automaatti ON osoite1 = osoite
SELECT osoite2, latitude, longitude FROM reitti JOIN paketti ON " + pktID + " = reittiID JOIN automaatti ON osoite2 = osoite
SELECT * FROM paketti JOIN esine ON esineID = pakettiID JOIN luokka ON paketti.ID = luokka.ID
SELECT ID FROM paketti WHERE pakettiID = ?
SELECT * FROM luokka WHERE ID = ?
SELECT * FROM automaatti JOIN reitti ON osoite1 = osoite JOIN paketti ON " + pktID + " = reittiID WHERE osoite1 = osoite
SELECT * FROM automaatti JOIN reitti ON osoite2 = osoite JOIN paketti ON " + pktID + " = reittiID WHERE osoite2 = osoite

SELECT asiakasID FROM asiakas WHERE paketti.asiakasID = asiakas.asiakasID
SELECT osoite FROM automaatti WHERE nimi = ?
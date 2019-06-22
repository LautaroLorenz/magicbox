Capturas
1- Con el bluetooth apagado
2- Con el bluetooth activo puedo buscar dispositivos
3- Buscando dispositivos
4- Dispositivos que encontre, puedo vincularme o desvincularme. Luego voy al inicio
5- Elijo "Ver dispositivos emparejados". Puedo conectarme a la magicbox.
6- Elijo un producto del listado
7- Veo el contenedor, aca entran en juego los sensores
8- Mapa con proveedores del producto


Mensajes de los sensores
Sensor de proximidad: al detectar presencia, envía una "T" al arduino

Giroscopio: al girarlo en sentido antihorario en el eje Z, envia una "V" al arduino
al girarlo en sentido horario en el eje Z, envia una "P" al arduino

Microfono: 
Al detectar la palabra "alarma", envia una "B"
Al detectar la palabra "estado", envia una "E"
Al detectar la palabra "puerta", envia una "Z"

Aclaracion: un thread en la app se encarga de estar leyendo constantemente el buffer del bluetooth y actualizando la interfaz
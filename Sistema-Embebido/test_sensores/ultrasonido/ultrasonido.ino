#include <NewPing.h>

/*Aqui se configuran los pines donde debemos conectar el sensor*/
#define TRIGGER_PIN  12
#define ECHO_PIN     11
#define TRIGGER_PIN2  9
#define ECHO_PIN2     10
#define MAX_DISTANCE 200

#define TRIGGER_PIN3  7
#define ECHO_PIN3     8

/*Crear el objeto de la clase NewPing*/
NewPing sonar(TRIGGER_PIN, ECHO_PIN, MAX_DISTANCE);
NewPing sonar2(TRIGGER_PIN2, ECHO_PIN2, MAX_DISTANCE);
NewPing sonar3(TRIGGER_PIN3, ECHO_PIN3, MAX_DISTANCE);

void setup() {
  Serial.begin(9600);
}

void loop() {
  // Esperar 1 segundo entre mediciones
  delay(3000);
  // Obtener medicion de tiempo de viaje del sonido y guardar en variable uS
  int uS = sonar.ping_median();
  // Imprimir la distancia medida a la consola serial
  Serial.print("Distancia: ");
  // Calcular la distancia con base en una constante
  Serial.print(uS / US_ROUNDTRIP_CM);
  Serial.println("cm");
  
  int uS2 = sonar2.ping_median();
  Serial.print("Distancia 2: ");
  Serial.print(uS2 / US_ROUNDTRIP_CM);
  Serial.println("cm");

  int uS3 = sonar3.ping_median();
  Serial.print("Distancia 3: ");
  Serial.print(uS3 / US_ROUNDTRIP_CM);
  Serial.println("cm");
}

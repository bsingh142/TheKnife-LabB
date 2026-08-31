Leggere il manuale utente per la corretta installazione e esecuzione

A causa di un problema nella condivisione della javadoc:
-Scaricare e installare Maven seguendo le istruzioni al seguente link https://maven.apache.org/install.html
-Dopo essersi assicurati di aver scaricato Maven, spostarsi nella cartella del progetto usando il comando cd da terminale ed eseguire questo comando mvn javadoc:javadoc
La documentazione javadoc sarà sotto la cartella target\reports\apidocs
Se invece si volesse creare altri file .jar oltre a quelli forniti, spostarsi nella cartella del progetto con il comando cd da terminale ed eseguire il comando mvn clean package

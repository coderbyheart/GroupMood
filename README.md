# GroupMood

_GroupMood_ ist eine Anwendung für Android und ermöglich es, eine Gruppen von Personen schnell und direkt mit Hilfe ihres Smartphones zu befragen. 

Die Anwendung ist im Kurs Mobile-Computing im 5. Semester des [Medieninformatik-Studiums an der Hochschule RheinMain](http://www.hs-rm.de/medieninformatik) entstanden. 

## Autoren

* [Coralie Reuter](http://github.com/CoralBlack) m@coderbyheart.de
* [Markus Tacker](http://github.com/tacker) coralie.reuter@hrcom.de

## Dokumentation

Im [Wiki](https://github.com/tacker/GroupMood/wiki) findet sich die ausführliche Beschreibung des Projekts. Die PDF-Version dieser Dokumentation ist [ebenfalls verfügbar](https://github.com/tacker/GroupMood/raw/master/documents/GroupMood.pdf). [Diese Präsentation](https://github.com/tacker/GroupMood/raw/master/documents/GroupMoodPräsentation.pdf) gibt einen kompakten Überblick über die Anwendung.

## Android-App

Unter ``market/GroupMood.apk`` findet sich die exportierte Anwendung für Android (ab Version 2.1, API Level 7).
Sie ist auch [im Android Market verfügbar](https://market.android.com/details?id=de.hsrm.mi.mobcomp.y2k11grp04).
Der Quellcode dafür findet sich unter ``moodclient``.  
Screenshots finden sich im Ordner ``market`` oder in [diesem Flickr-Set](http://www.flickr.com/photos/tacker/sets/72157629061730644).

## Server

Unter ``moodserver`` findet sich ein auf Django 1.3 basierter Server der zur Verwendung der Anwendung nötig ist. Unter ``moodserver/README`` findet sich eine Anleitung, wie man den Server startet.

## Verwendete Quellen

Die Software verwendet zum Teil fremde Inhalte:

* [Barcode-Scanner-Komponente](http://code.google.com/p/zxing/source/browse/trunk/android-integration/src/com/google/zxing/integration/android/) von ZXing
* [HorizontalListView und zugehörige Komponenten](https://github.com/fry15/uk.co.jasonfry.android.tools) von Jason Fry

Die Icons in der ActionBar basieren auf den Icons:

* [Icon 876](http://thenounproject.com/noun/check-box/#icon-No876) von Hrag Chanchanian von [The Noun Project](http://www.thenounproject.com/)
* [Icon 460](http://thenounproject.com/noun/blog/#icon-No460) von [The Noun Project](http://www.thenounproject.com/)
* [Icon 805](http://thenounproject.com/noun/pie-chart/#icon-No805) von Scott Lewis von [The Noun Project](http://www.thenounproject.com/)
* [Icon 253](http://thenounproject.com/noun/share/#icon-No253) von [The Noun Project](http://www.thenounproject.com/)

## Lizenz

Der Quellcode ist unter der GPL lizenziert.


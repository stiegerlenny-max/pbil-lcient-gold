Pbil Client 1.0 (Fabric, Minecraft 1.21.11)
===========================================

BAUEN: Inhalt dieses Ordners (mit dem versteckten Ordner .github) in ein GitHub-Repository hochladen.
Unter "Actions" laeuft der Build, die fertige Jar liegt dann bei "Artifacts" (pbil-client).
Die Jar in den mods-Ordner legen. Fabric API wird gebraucht.

BENUTZEN: Rechts-Shift oeffnet das Menue (Taste aenderbar unter Steuerung -> Pbil Client).
Klick auf ein Modul = an/aus, ">" = Einstellungen. Einstellungen werden in config/pbil_client.properties gespeichert.

MODULE
  Combat   KillAura, TriggerBot, CrystalAura, AutoTotem, Surround
  Movement Sprint, Speed, Fly, Step, NoFall, AutoWalk, AutoJump
  Player   AutoRespawn, AutoEat, AntiAFK
  Render   PlayerESP, MobESP, ItemESP, CrystalESP, StorageESP, SpawnerESP, BlockESP, Tracers, Nametags, Fullbright, Zoom
  HUD      Watermark, ModuleList, Coordinates, ArmorHUD, PvPInfo, Speed (BPS)

HINWEISE
  - ESP, Tracer und Nametags werden als 2D-Overlay gezeichnet (nur in der Ego-Ansicht), nicht durch den Renderer.
  - Viele Server verbieten Combat-/Movement-Module. Nutze sie nur dort, wo es erlaubt ist.

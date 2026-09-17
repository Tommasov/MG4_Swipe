# MG4 Swipe Launcher

<p align="center">
  <img src="https://ws2.tommasovietina.it/mg4/MG4_Swipe.png" alt="MG4 Swipe Launcher" width="200" />
</p>

MG4_Swipe_NovaLauncher is an app that enables a **swipe up** action from the bottom edge of the screen to quickly launch a user-selected app. This feature is especially useful for fast and easy access to a specific app of your choice.

> 💡 It pairs perfectly with [MG4 Simple Launcher](https://github.com/Tommasov/MG4_Simple_Launcher) — which is now the **default** swipe target. As the home launcher it stays warm in memory, so the swipe re-opens it almost instantly, for a clean, system-integrated home experience.

## ⚠️ Upgrading from v1.2 (or earlier) — please read

**EN —** Starting from **v1.4** the app is signed with a new, stable signing key.
Android does **not** allow installing an update over an app signed with a different
key, so you cannot update on top of an older build: you must **uninstall the
previous version first**, then install v1.4. This is a **one-time** step needed only
to migrate to the new signed builds — every future update will install normally,
with no uninstall. (Your settings are stored per-app, so they are reset on
reinstall.)

**IT —** A partire dalla **v1.4** l'app è firmata con una nuova chiave di firma
stabile. Android **non** consente di installare un aggiornamento sopra un'app
firmata con una chiave diversa, quindi non puoi aggiornare sopra una versione
precedente: devi prima **disinstallare la versione vecchia** e poi installare la
v1.4. È un passaggio **una tantum**, necessario solo per migrare alle nuove build
firmate — tutti gli aggiornamenti futuri si installeranno normalmente, senza
disinstallare. (Le impostazioni sono salvate per app, quindi verranno azzerate con
la reinstallazione.)

## Features

- **Swipe up from the bottom edge**: Quickly launch your chosen app with a simple swipe-up gesture.
- **Default target — MG4 Simple Launcher**: out of the box the swipe opens [MG4 Simple Launcher](https://github.com/Tommasov/MG4_Simple_Launcher) (`com.tommasov.mg4simplelauncher`). Being the home launcher it stays warm, so it re-opens almost instantly.
- **Any app works too**: Nova Launcher (or any installed app) can be chosen instead of the default from the MG4 Swipe main screen.
- **Configuration**: Select the app you want to launch by opening the MG4 Swipe app.

## Settings

These options are configurable from the MG4 Swipe main screen:

- **Two swipe areas**: the bottom edge is split into a left and a right area. By
  default the left area triggers the back action (simulated physical back button)
  and the right area opens the selected app.
- **Swap the swipe areas (left ↔ right)**: exchanges the back and open actions
  between the two areas. The on-screen help labels at the bottom follow this
  setting — both their text **and** their background colour swap accordingly, so
  they always match the active layout.
- **Hide the floating back button**: hides the draggable floating back button,
  useful for car servicing (hidden by default).
- **Show a loader while opening**: shows a lightweight "Opening…" overlay over the
  launch transition. It gives immediate feedback that the swipe was registered and
  masks the brief window flash while the target app is brought to the foreground
  (off by default).
- **App version**: the installed version name is shown in the top corner of the
  main screen.
  
## Videoguida in italiano

Una guida video dedicata al pubblico italiano, che mostra il launcher in funzione
sulla MG4:

<p align="center">
  <a href="https://www.youtube.com/watch?v=XDgJUkOuVAg">
    <img src="https://img.youtube.com/vi/XDgJUkOuVAg/maxresdefault.jpg" alt="MG4 Swipe, e le nuove APP compaiono!!! — videoguida in italiano" width="640" />
  </a>
</p>

<p align="center">
  <a href="https://www.youtube.com/watch?v=XDgJUkOuVAg"><strong>MG4 Swipe, e le nuove APP compaiono!!!</strong></a>
</p>

> 🇬🇧 *Italian-language video guide showing the launcher running on the MG4 head unit.*

## License

MG4 Swipe Launcher is free software: you can redistribute it and modify it under
the terms of the **GNU General Public License, version 3 or later**, as published
by the Free Software Foundation. The full text is in [`LICENSE`](LICENSE).

    Copyright (C) 2024 Tommaso Vietina

    This program is distributed in the hope that it will be useful, but WITHOUT ANY
    WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
    PARTICULAR PURPOSE. See the GNU General Public License for more details.

Anyone may use this, change it and publish their own version — and a published
version has to carry its source under the same licence.

The licence covers the code written for this project. Graphic resources, trademarks
and brand names belong to their respective owners, as set out below.

## Disclaimer (English)

This project is provided **for study and educational purposes only**. It is an
experimental, non-commercial project and is not affiliated with, endorsed by, or
supported by SAIC, MG, Nova Launcher, or any vehicle manufacturer.

The software is provided "as is", without warranty of any kind, express or
implied. The author accepts **no liability** for any direct, indirect, incidental,
or consequential damage of any kind — including but not limited to damage to the
vehicle, its infotainment system, software, or data, loss of functionality, or
safety-related consequences — arising from the installation or use of this app.
You use it entirely **at your own risk**. Do not interact with the app while
driving.

All graphic resources, trademarks, and brand names belong to their respective
owners and are used here for study purposes only.

## Avvertenze (Italiano)

Questo progetto è fornito **esclusivamente a scopo di studio ed educativo**. È un
progetto sperimentale, non commerciale, non affiliato né approvato o supportato da
SAIC, MG, Nova Launcher o da alcun costruttore di veicoli.

Il software è fornito "così com'è", senza garanzie di alcun tipo, esplicite o
implicite. L'autore non si assume **alcuna responsabilità** per qualsiasi danno
diretto, indiretto, incidentale o consequenziale — incluso, a titolo
esemplificativo, danni al veicolo, al sistema di infotainment, al software o ai
dati, perdita di funzionalità o conseguenze relative alla sicurezza — derivante
dall'installazione o dall'uso di questa app. L'utilizzo avviene interamente **a
proprio rischio**. Non interagire con l'app durante la guida.

Tutte le risorse grafiche, i marchi e i nomi commerciali appartengono ai
rispettivi proprietari e sono utilizzati qui solo a scopo di studio.

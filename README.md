# Zgadnij kraj po fladze

Prosta gra w jednej stronie HTML — wpisz nazwę kraju po polsku na podstawie flagi.

Jak opublikować na GitHub Pages:

1. Utwórz nowe repozytorium na GitHub (np. `flags-game`).
2. Dodaj remote i wypchnij gałąź `main`:

```bash
git remote add origin git@github.com:USERNAME/REPO.git
git push -u origin main
```

Po wypchnięciu workflow GitHub Actions uruchomi się automatycznie i wdroży stronę na Pages.

Alternatywnie możesz użyć `gh` CLI:

```bash
gh repo create USERNAME/REPO --public --source=. --remote=origin
git push -u origin main
```

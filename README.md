## Marmita Delivery App

Aplicativo web + Android para delivery de marmitas, com cadastro, checkout, perfil e área administrativa.

### Stack Web
- Vite + React + TypeScript
- Tailwind CSS + shadcn-ui
- Supabase (auth, storage e API)

### Como rodar (web)
Requisitos: Node.js 18+

```sh
npm i
npm run dev
```

### Variáveis de ambiente
Crie um arquivo `.env` baseado em `.env.example` e preencha com as chaves do Supabase.

### Como buildar (web)
```sh
npm run build
npm run preview
```

### Android (Compose)
O app Android está em `android-app/`. Use Android Studio ou:

```sh
cd android-app
./gradlew assembleDebug
```

### Licença
Este projeto é de uso interno da marca/cliente. Consulte o autor para redistribuição.

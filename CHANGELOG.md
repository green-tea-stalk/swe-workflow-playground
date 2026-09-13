# Changelog

## [0.2.0](https://github.com/green-tea-stalk/swe-workflow-playground/compare/v0.1.0...v0.2.0) (2026-09-13)


### Features

* **app:** configure root npm workspace and consolidated dev scripts ([cd3196d](https://github.com/green-tea-stalk/swe-workflow-playground/commit/cd3196ddf61cd8d028918000ac9462b3fe7bef23))
* **backend:** implement COMP-001 PostController and RFC 9457 error handling ([727d94a](https://github.com/green-tea-stalk/swe-workflow-playground/commit/727d94aa5d1a1005327323654dbd19910a1f20e3))
* **backend:** implement COMP-002 PostService domain logic ([4326d2f](https://github.com/green-tea-stalk/swe-workflow-playground/commit/4326d2f26f9266a16c443c3890c15d89b19b2734))
* **backend:** implement COMP-003 PostEntity DTOs and PostRepository ([dbfcd02](https://github.com/green-tea-stalk/swe-workflow-playground/commit/dbfcd0224ffca11b48cdc43df5ba70a06d81fb5e))
* **backend:** implement HttpLocaleResolver for Accept-Language negotiation ([1464a34](https://github.com/green-tea-stalk/swe-workflow-playground/commit/1464a343b997e655e4256a46b33f078b7a710e11))
* **backend:** implement MessageLocalizationService and resource bundles ([e8d2ef1](https://github.com/green-tea-stalk/swe-workflow-playground/commit/e8d2ef106f886dfc9caba1f49d580a7673b1343b))
* **backend:** localize RFC 9457 exception handlers with Accept-Language support ([5917f20](https://github.com/green-tea-stalk/swe-workflow-playground/commit/5917f209ed063ca5a7d2232adf8a2e67b0a4d97a))
* **frontend:** implement COMP-004 PostFeedComponent with pagination ([ed5b00e](https://github.com/green-tea-stalk/swe-workflow-playground/commit/ed5b00e693b0805dfbaf0c3dd7969c34f2b2a571))
* **frontend:** implement COMP-005 PostFormComponent with fixed bottom layout ([273c76e](https://github.com/green-tea-stalk/swe-workflow-playground/commit/273c76e2f4e92100bce10df5cd9e0dd705bc1975))
* **frontend:** implement LanguageSwitchComponent in navigation toolbar ([472baa7](https://github.com/green-tea-stalk/swe-workflow-playground/commit/472baa73dbab126b9efac12decb4eb460ba08677))
* **frontend:** implement LocaleService for discovery, persistence, and navigation ([da8552a](https://github.com/green-tea-stalk/swe-workflow-playground/commit/da8552ad437ea7b83335e15a7db92b60ea46a912))
* **frontend:** implement persistent bottom form and complete E2E integration ([a112e9c](https://github.com/green-tea-stalk/swe-workflow-playground/commit/a112e9c3bbbbd6fb630f4d699f55710b7fb381ce))
* **frontend:** implement view localization, translation bundles, and bilingual e2e tests ([03b0444](https://github.com/green-tea-stalk/swe-workflow-playground/commit/03b0444ddf04809d9eb6f7dd48dc11ded2a8c18b))
* **frontend:** inject Accept-Language header in PostApiService calls ([7ea18d3](https://github.com/green-tea-stalk/swe-workflow-playground/commit/7ea18d3afe05cc6ccce7a9a62915515ee7212e6d))
* **frontend:** localize PostFeedComponent and configure DatePipe locale formatting ([422d943](https://github.com/green-tea-stalk/swe-workflow-playground/commit/422d9438557660acd4ff42767422212d48a64b87))
* **frontend:** localize PostFormComponent inputs, errors, and snackbars ([e7519c8](https://github.com/green-tea-stalk/swe-workflow-playground/commit/e7519c842c4195219b28a40d90bb9ba99abd1027))


### Bug Fixes

* **app:** correct backend mainClass and configure dev-server API proxy ([2054b5a](https://github.com/green-tea-stalk/swe-workflow-playground/commit/2054b5a35773cd1a9c369e0cc70b9f141dbb3966))
* **ci:** pin frontend typescript updates to patch versions in dependabot ([35143af](https://github.com/green-tea-stalk/swe-workflow-playground/commit/35143af90b33abf0280a58123e1ccb247cab7b13))
* **ci:** pin frontend typescript updates to patch versions in dependabot ([724bf3b](https://github.com/green-tea-stalk/swe-workflow-playground/commit/724bf3ba49bad7e89a6da29ed54a7b1db25890ef))
* **ci:** start and await containerized mysql in backend verification job ([dce90a7](https://github.com/green-tea-stalk/swe-workflow-playground/commit/dce90a769e8ff283dfb2bf0e3b2a54da113e8a2b))
* **ci:** start and await containerized mysql in backend verification job ([9409183](https://github.com/green-tea-stalk/swe-workflow-playground/commit/94091838c89ec1b38c25fa96b4e59ff79c4b4463))
* **frontend:** implement multi-locale distribution server for live language switching ([ce56941](https://github.com/green-tea-stalk/swe-workflow-playground/commit/ce56941f8f476c8e73efc596ac7ddff936881c47))
* **frontend:** resolve feed scroll clipping and add Playwright E2E suite ([dcec7e6](https://github.com/green-tea-stalk/swe-workflow-playground/commit/dcec7e6a24843b99940b57b583b3dba156b48f20))
* **specs:** quote edge labels with parentheses in requirements mermaid diagrams ([2730f21](https://github.com/green-tea-stalk/swe-workflow-playground/commit/2730f21612cadb056cb9e207d9c8fdc18775ab38))

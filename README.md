# wildmatch-kotlin in Kotlin

[![GitHub link](https://img.shields.io/badge/GitHub-KotlinMania%2Fwildmatch--kotlin-blue.svg)](https://github.com/KotlinMania/wildmatch-kotlin)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.kotlinmania/wildmatch-kotlin)](https://central.sonatype.com/artifact/io.github.kotlinmania/wildmatch-kotlin)
[![Build status](https://img.shields.io/github/actions/workflow/status/KotlinMania/wildmatch-kotlin/ci.yml?branch=main)](https://github.com/KotlinMania/wildmatch-kotlin/actions)

This is a Kotlin Multiplatform line-by-line transliteration port of [`becheran/wildmatch`](https://github.com/becheran/wildmatch).

**Original Project:** This port is based on [`becheran/wildmatch`](https://github.com/becheran/wildmatch). All design credit and project intent belong to the upstream authors; this repository is a faithful port to Kotlin Multiplatform with no behavioural changes intended.

### Porting status

This is an **in-progress port**. The goal is feature parity with the upstream Rust crate while providing a native Kotlin Multiplatform API. Every Kotlin file carries a `// port-lint: source <path>` header naming its upstream Rust counterpart so the AST-distance tool can track provenance.

---

## Upstream README — `becheran/wildmatch`

> The text below is reproduced and lightly edited from [`https://github.com/becheran/wildmatch`](https://github.com/becheran/wildmatch). It is the upstream project's own description and remains under the upstream authors' authorship; links have been rewritten to absolute upstream URLs so they continue to resolve from this repository.

## wildmatch

[![build status](https://github.com/becheran/wildmatch/workflows/Build/badge.svg)](https://github.com/becheran/wildmatch/actions?workflow=Build)
[![docs](https://img.shields.io/docsrs/wildmatch/latest)](https://docs.rs/wildmatch/latest/wildmatch/)
[![downloads](https://img.shields.io/crates/v/wildmatch.svg?color=orange)](https://crates.io/crates/wildmatch)
[![crate](https://badgen.net/crates/d/wildmatch)](https://crates.io/crates/wildmatch)
[![license](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/license/MIT)
[![codecov](https://img.shields.io/codecov/c/github/becheran/wildmatch/master)](https://app.codecov.io/gh/becheran/wildmatch)

Match strings against a simple wildcard pattern. Tests a wildcard pattern `p` against an input string `s`. Returns true only when `p` matches the entirety of `s`.

See also the example described on [wikipedia](https://en.wikipedia.org/wiki/Matching_wildcards) for matching wildcards.

- `?` matches exactly one occurrence of any character.
- `*` matches arbitrary many (including zero) occurrences of any character.
- No escape characters are defined.

Can also be used with a [custom match pattern](https://docs.rs/wildmatch/latest/wildmatch/struct.WildMatchPattern.html) to define own wildcard patterns for single and multi-character matching.

For example the pattern `ca?` will match `cat` or `car`. The pattern `https://*` will match all https urls, such as `https://google.de` or `https://github.com/becheran/wildmatch`.

The following table shows a performance benchmarks between wildmatch, [regex](https://crates.io/crates/regex),[glob](https://docs.rs/glob/0.3.0/glob/struct.Pattern.html), and the [regex_lite](https://github.com/rust-lang/regex/tree/master/regex-lite) libraries:

| Benchmark         | wildmatch     | regex      | glob           | regex_lite
| ----              | ------------: | ---------: | -------------: | ---------:
| compiling/text    |    **462 ns** |  39,714 ns |   1,470 ns     | 13,210 ns
| compiling/complex |     190 ns    | 153,830 ns |     238 ns     | **60 ns**
| matching/text     |    **186 ns** |   4,065 ns |     456 ns     | 6,097 ns
| matching/complex  |    **310 ns** |  16,085 ns |   1,426 ns     | 3,773 ns

The library only depends on the rust [`stdlib`](https://doc.rust-lang.org/std/).

See the [documentation](https://docs.rs/wildmatch/latest/wildmatch/) for usage and more examples.

---

## About this Kotlin port

### Installation

```kotlin
dependencies {
    implementation("io.github.kotlinmania:wildmatch-kotlin:0.1.0")
}
```

### Building

```bash
./gradlew build
./gradlew test
```

### Targets

- macOS arm64
- Linux x64 / arm64
- Windows mingw-x64
- iOS arm64 / x64 / simulator-arm64 (Swift export + XCFramework)
- JS (browser + Node.js)
- Wasm-JS (browser + Node.js)
- Android (API 24+)

### Porting guidelines

See [AGENTS.md](AGENTS.md) and [CLAUDE.md](CLAUDE.md) for translator discipline, port-lint header convention, and Rust → Kotlin idiom mapping.

### License

This Kotlin port is distributed under the same MIT license as the upstream [`becheran/wildmatch`](https://github.com/becheran/wildmatch). See [LICENSE](LICENSE) (and any sibling `LICENSE-*` / `NOTICE` files mirrored from upstream) for the full text.

Original work copyrighted by the wildmatch authors.  
Kotlin port: Copyright (c) 2026 Sydney Renee and The Solace Project.

### Acknowledgments

Thanks to the [`becheran/wildmatch`](https://github.com/becheran/wildmatch) maintainers and contributors for the original Rust implementation. This port reproduces their work in Kotlin Multiplatform; bug reports about upstream design or behavior should go to the upstream repository.

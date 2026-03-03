# Lyrics ToS / Legal Notes

This project includes only a **controlled pipeline design** for lyrics retrieval.

## Compliance principles
- Respect each target source Terms of Service.
- Prefer official licensed APIs over scraping.
- Do not bypass anti-bot controls or access controls.
- Cache with conservative TTL and source attribution metadata.
- Allow source-level disable switches in production builds.

## Recommended production approach
1. Use a licensed lyrics API provider.
2. Keep the confidence-scoring validation layer.
3. Keep local caching and failed-match suppression.
4. Keep fully asynchronous behavior so playback never depends on lyric availability.

## Suggested API-first architecture
- Provider adapter interface (`LyricsSource`) remains unchanged.
- Replace scraper implementations with API client adapters.
- Add per-provider quotas, backoff, and request signing.
- Add legal review checklists before rollout.

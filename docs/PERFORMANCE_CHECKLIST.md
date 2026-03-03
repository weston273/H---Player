# Performance Checklist

## Scanning and indexing
- [x] Query MediaStore on background threads
- [x] Use incremental watermark scans (DATE_MODIFIED)
- [x] Emit scan progress events asynchronously
- [x] Keep ingestion batched inside Room transaction
- [ ] Add periodic maintenance full reconciliation worker

## Playback reliability
- [x] Playback in MediaSessionService
- [x] Audio focus configured via Media3 attributes
- [x] Queue persisted across process death
- [x] Playback state restoration on relaunch
- [x] RN state fed by native event stream

## Metadata and artwork
- [x] MediaStore-first metadata
- [x] Retriever fallback using file descriptors
- [x] Artwork decode off main thread
- [x] Memory + disk cache strategy
- [x] LRU eviction path

## Large libraries
- [x] Paginated queries for songs
- [x] Virtualized RN lists
- [x] Indexed DB lookups for ranking views
- [x] Recommendation cache to avoid full recompute every request

## Safety and resilience
- [x] Failed lyrics matches cached
- [x] Timeout + retry for lyrics retrieval
- [x] Confidence threshold before acceptance
- [ ] Add richer codec capability probes per device class
- [ ] Add structured crash and ANR telemetry

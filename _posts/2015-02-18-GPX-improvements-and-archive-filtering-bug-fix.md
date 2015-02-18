---
layout: default
---

Now export to GPX contains `address`, `speed`, `course` and `power` data under `extensions` node in `traccar` namespace. Import is aware of them, so now GPX can be used as interchange format between several traccar servers.

Next, all values under `extensions` will be put into `info` field of every positions, nested elements like `<E1><E2><E3>value</E3></E2></E1>` will be combined in a single field with dashes like `<E1-E2-E3>value</E1-E2-E3>`.

Then, import will update latest position of device if there is no position set up yet, or there is a newer position imported than already existing.

Also import now has several unit tests.

Archive filtering had an issue when 'Disable filter' is checked - 'Hide duplicates' and 'Hide positions with distance less than' were still applied.
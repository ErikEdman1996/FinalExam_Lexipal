delete from user_achievements ua
using achievement_definitions ad
where ua.achievement_id = ad.id
  and (ad.code like 'CODEX_%' or ad.metric_key = 'codex_words');

delete from achievement_definitions
where code like 'CODEX_%'
   or metric_key = 'codex_words';

update store_item_definitions
set code = 'forest_canopy',
    name = 'Forest Canopy',
    description = 'Dark forest greens for quiet evening reading.'
where code = 'forest_codex';

update theme_definitions
set code = 'forest_canopy',
    name = 'Forest Canopy',
    description = 'Dark forest greens for quiet evening reading.'
where code = 'forest_codex';

alter table challenge_attempts
    drop column if exists codex_entry_id;

alter table sessions
    drop column if exists captured_word_count;

drop table if exists codex_entries;

update portrait_definitions
set required_level = case code
    when 'larvae_starter' then 1
    when 'curious_larvae' then 3
    when 'silken_cocoon' then 5
    when 'emerging_moth' then 7
    when 'young_moth' then 9
    when 'lexipal_moth' then 10
    else required_level
end
where code in (
    'larvae_starter',
    'curious_larvae',
    'silken_cocoon',
    'emerging_moth',
    'young_moth',
    'lexipal_moth'
);

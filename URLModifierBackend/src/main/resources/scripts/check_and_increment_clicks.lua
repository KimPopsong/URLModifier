-- 클릭 수 한도를 원자적으로 검사하고, 한도 내이면 카운터를 증가시킨다.
-- 검사(GET)와 증가(INCR)를 하나의 스크립트로 묶어 동시 요청 간 경쟁 조건을 제거한다.
--
-- KEYS[1]: 클릭 카운터 키 (예: url:clicks:123)
-- ARGV[1]: maxClicks 한도
--
-- 반환값:
--   -2  : 카운터가 아직 존재하지 않음 (Java에서 DB 조회로 초기화 후 재시도해야 함)
--   -1  : 한도 초과 (카운터 변화 없음)
--   N>0 : 허용됨, 증가 후 새 카운터 값

local current = redis.call('GET', KEYS[1])
if current == false then
    return -2
end

current = tonumber(current)
local max = tonumber(ARGV[1])

if current >= max then
    return -1
end

return redis.call('INCR', KEYS[1])

package com.example.recovery.maker;

import com.example.recovery.domain.memoirs.Memoirs;
import com.example.recovery.domain.user.Users;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.LocalDate;
import java.util.Map;

public class MemoirsMaker {

    private static final String MEMOIR_JSON = """
            {
              "1": {
            	"title": "2026/01/01 회고 - 오늘 공부한 것",
            	"subMemoirTitles": {
            	  "1": {
            		"title": "TSX란?"
            	  }
            	}
              },
              "2": {
            	"title": "2026/03/09 회고 - 오늘의 나의 일기",
            	"subMemoirTitles": {
            	  "1": {
            		"title": "문구점을 갔다."
            	  },
            	  "2": {
            		"title": "산책을 갔다."
            	  }
            	}
              }
            }
            """;

    private static final String IMPROVEMENT_JSON = """
            {
              "1": {
            	"improvement": "TSX를 공부했다.",
            	"subImprovements": {
            	  "1": {
            		"improvement": "TSX는 React 컴포넌트 정의를 위한 TypeScript기반 파일이다."
            	  }
            	}
              }
            }
            """;

    private static final String FEEDBACK_JSON = """
            {
              "1": {
            	"feedback": "TSX를 공부했다.",
            	"subFeedback": {
            	  "1": {
            		"feedback": "TSX는 React 컴포넌트 정의를 위한 TypeScript기반 파일이다."
            	  }
            	}
              }
            }
            """;

    private final TestEntityManager entityManager;
    private final JsonMaker jsonMaker;

    public MemoirsMaker(TestEntityManager entityManager) {
        this.entityManager = entityManager;
        this.jsonMaker = new JsonMaker();
    }

    public void persist(Users user, LocalDate date) {
        Memoirs memoir = new Memoirs();
        memoir.setUsers(user);
        memoir.setMemoir(jsonMaker.parseJsonToMap(MEMOIR_JSON));
        memoir.setImprovement(jsonMaker.parseJsonToMap(IMPROVEMENT_JSON));
        memoir.setFeedback(jsonMaker.parseJsonToMap(FEEDBACK_JSON));
        memoir.setDate(date);
        entityManager.persist(memoir);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> asMap(Object value) {
        return (Map<String, Object>) value;
    }

    public Object nestedValue(Object root, String... keys) {
        Object current = root;
        for (String key : keys) {
            current = asMap(current).get(key);
        }
        return current;
    }
}



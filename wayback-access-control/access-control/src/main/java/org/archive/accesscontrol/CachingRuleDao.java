package org.archive.accesscontrol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.archive.accesscontrol.model.RuleSet;

/**
 * CachingRuleDao is a wrapper for another RuleDao that implements in-memory
 * caching of the rule trees.
 * 
 * @author aosborne
 * 
 */
public class CachingRuleDao implements RuleDao {
    private static final long serialVersionUID = 1L;
    private static final int PREPARE_THREAD_COUNT = 5;
    protected RuleDao ruleDao;
    protected LruCache<String, RuleSet> cache = new LruCache<String, RuleSet>();

    public CachingRuleDao(RuleDao ruleDao) {
        super();
        this.ruleDao = ruleDao;
    }

    public CachingRuleDao(String oracleUrl) {
        this(new HttpRuleDao(oracleUrl));
    }

    public RuleDao getRuleDao() {
        return ruleDao;
    }

    public void setRuleDao(RuleDao ruleDao) {
        this.ruleDao = ruleDao;
    }

    public RuleSet getRuleTree(String surt) throws RuleOracleUnavailableException {
        RuleSet rules;
        synchronized (cache) {
            rules = cache.get(surt);
        }
        if (rules == null) {
            rules = ruleDao.getRuleTree(surt);
            synchronized (cache) {
                cache.put(surt, rules);
            }
        }
        return rules;
    }

    class FetchThread extends Thread {
        private List<String> surts;

        public FetchThread(List<String> surts) {
            this.surts = surts;
        }

        public void run() {
            while (true) {
                String surt;
                synchronized (surts) {
                    if (surts.isEmpty())
                        break;
                    surt = surts.remove(0);
                }
                try {
                    getRuleTree(surt);
                } catch (RuleOracleUnavailableException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Prepare the cache to lookup info for a given set of surts. The fetches
     * happen in parallel so this also makes a good option for speeding up bulk lookups.
     * 
     * @param surts
     */
    public void prepare(Collection<String> surts) {
        List<String> safeSurts = new ArrayList<String>(surts);
        FetchThread threads[] = new FetchThread[PREPARE_THREAD_COUNT ];
        for (int i = 0; i < PREPARE_THREAD_COUNT ; i++) {
            threads[i] = new FetchThread(safeSurts);
            threads[i].start();
        }
        for (int i = 0; i < PREPARE_THREAD_COUNT ; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
            }
        }
    }
}

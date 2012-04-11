package org.archive.accesscontrol.model;

import java.util.Date;

/**
 * The oracle keeps track of the history of rules by recording each change. This
 * class describes a single change (create, update or delete).
 * 
 * @author aosborne
 * 
 */
public class RuleChange extends Rule {
    public static final String CREATED = "created";
    public static final String UPDATED = "updated";
    public static final String DELETED = "deleted";

    private Date changeDate;
    private String changeUser;
    private String changeComment;
    private String changeType;
    private Long ruleId;

    public RuleChange() {
        super();
    }

    public RuleChange(Rule rule, String changeType, Date changeDate,
            String changeUser, String changeComment) {
        super();
        setRule(rule);
        setChangeType(changeType);
        setChangeDate(changeDate);
        setChangeUser(changeUser);
        setChangeComment(changeComment);
        copyFrom(rule);
    }

    /**
     * @return the date the rule was replaced.
     */
    public Date getChangeDate() {
        return changeDate;
    }

    /**
     * @param changeDate
     *            the changeDate to set
     */
    public void setChangeDate(Date changeDate) {
        this.changeDate = changeDate;
    }

    /**
     * @return the user who changed the rule
     */
    public String getChangeUser() {
        return changeUser;
    }

    /**
     * @param changeUser
     *            the user who changed the rule
     */
    public void setChangeUser(String changeUser) {
        this.changeUser = changeUser;
    }

    /**
     * @return a comment describing the change
     */
    public String getChangeComment() {
        return changeComment;
    }

    /**
     * @param changeComment
     *            a comment describing the change
     */
    public void setChangeComment(String changeComment) {
        this.changeComment = changeComment;
    }

    /**
     * @param rule
     *            the rule to set
     */
    public void setRule(Rule rule) {
        if (rule == null) {
            setRuleId(null);
        } else {
            setRuleId(rule.getId());
        }
    }

    /**
     * @return the changeType
     */
    public String getChangeType() {
        return changeType;
    }

    /**
     * @param changeType
     *            the changeType to set
     */
    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }

    /**
     * @return the ruleId
     */
    public Long getRuleId() {
        return ruleId;
    }

    /**
     * @param ruleId
     *            the ruleId to set
     */
    public void setRuleId(Long ruleId) {
        this.ruleId = ruleId;
    }

}

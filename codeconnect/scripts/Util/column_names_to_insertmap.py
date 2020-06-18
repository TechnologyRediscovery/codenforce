import re
def col_to_map(cols):
    """
        example:
            >>> col_to_map('''
            >>>     eventid, category_catid, cecase_caseid, eventtimestamp,
            >>>     eventdescription, owner_userid, disclosetomunicipality, disclosetopublic,
            >>>     activeevent, notes'''
            >>> )
            %(eventid)s, %(category_catid)s, %(cecase_caseid)s, %(eventtimestamp)s, %(eventdescription)s, %(owner_userid)s, %(disclosetomunicipality)s, %(disclosetopublic)s, %(activeevent)s, %(notes)s
    """
    new = re.sub(r"\b[a-zA-Z0-9_]+\b", r"\b[a-zA-Z0-9_]+\b", cols)
    word = r"\b[a-zA-Z0-9_]+\b"
    pattern = re.compile(word)
    match = re.findall(pattern, cols)
    words = []
    for w in match:
        words.append("%(" + w + ")s")
    return ", ".join(words)

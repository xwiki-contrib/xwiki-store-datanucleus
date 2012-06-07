public class DataNucleusNamedQueries
{
    String getWikiMacroDocuments = """
      SELECT space, name, author
        FROM org.xwiki.store.legacy.internal.datanucleus.PersistableXWikiDocument
        WHERE objects.contains(obj)
        && obj.className == "xwiki.XWiki.WikiMacroClass"
    """;

    String getWatchlistJobDocuments = """
      SELECT fullName
        FROM org.xwiki.store.legacy.internal.datanucleus.PersistableXWikiDocument
        WHERE objects.contains(obj)
        && obj.className == "xwiki.XWiki.WatchListJob"
    """;

    String getTranslationList = """
      SELECT language
        FROM org.xwiki.store.legacy.internal.datanucleus.PersistableXWikiDocument
        WHERE wiki == :wiki
          && fullName == :fullname
    """;
//          && language != \"\"

    String listGroupsForUser = """
      SELECT DISTINCT fullName
        FROM org.xwiki.store.legacy.internal.datanucleus.PersistableXWikiDocument
        WHERE objects.contains(obj)
        && obj.className == "xwiki.XWiki.XWikiGroups"
        && (
          obj.member == :username
          || obj.member == :shortname
          || obj.member == :veryshortname
        )
    """;

    String getSpaces = """
      SELECT DISTINCT space
        FROM org.xwiki.store.legacy.internal.datanucleus.PersistableXWikiDocument
    """;
}

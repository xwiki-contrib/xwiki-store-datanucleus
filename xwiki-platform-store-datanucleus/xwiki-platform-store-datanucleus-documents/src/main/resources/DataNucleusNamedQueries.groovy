public class DataNucleusNamedQueries
{
    String getWikiMacroDocuments = """
      SELECT space, name, author
        FROM org.xwiki.store.legacy.internal.datanucleus.PersistableXWikiDocument
        WHERE objects.contains(obj)
        VARIABLES xwiki.XWiki.WikiMacroClass
    """;

    String getWatchlistJobDocuments = """
      SELECT fullName
        FROM org.xwiki.store.legacy.internal.datanucleus.PersistableXWikiDocument
        WHERE objects.contains(obj)
        VARIABLES xwiki.XWiki.WatchListJob obj
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
        && (
          obj.member == :username
          || obj.member == :shortname
          || obj.member == :veryshortname
        )
        VARIABLES xwiki.XWiki.XWikiGroups obj
    """;

    String getSpaces = """
      SELECT DISTINCT space
        FROM org.xwiki.store.legacy.internal.datanucleus.PersistableXWikiDocument
    """;
}

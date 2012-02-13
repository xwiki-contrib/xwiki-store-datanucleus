public class DataNucleusNamedQueries
{

    String getWikiMacroDocuments = """
      SELECT space, name, author
        FROM com.xpn.xwiki.store.datanucleus.PersistableXWikiDocument
        WHERE objects.contains(obj)
          && obj instanceof xwiki.XWiki.WikiMacroClass
    """;

    String getWatchlistJobDocuments = """
      SELECT fullName
        FROM com.xpn.xwiki.store.datanucleus.PersistableXWikiDocument
        WHERE objects.contains(obj)
          && obj instanceof xwiki.XWiki.WatchListJob
    """;

    String getTranslationList = """
      SELECT language
        FROM com.xpn.xwiki.store.datanucleus.PersistableXWikiDocument
        WHERE wiki == :wiki
          && fullName == :fullname
    """;
//          && language != \"\"

    String listGroupsForUser = """
      SELECT DISTINCT fullName
        FROM com.xpn.xwiki.store.datanucleus.PersistableXWikiDocument
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
        FROM com.xpn.xwiki.store.datanucleus.PersistableXWikiDocument
    """;
}

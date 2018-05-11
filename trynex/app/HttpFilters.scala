import javax.inject.Inject
import play.api.http.HttpFilters
import play.filters.gzip.GzipFilter
import play.filters.csrf.CSRFFilter
import play.filters.headers.SecurityHeadersFilter
class Filters @Inject() (
    gzip: GzipFilter,
    csrf: CSRFFilter,
    securityHeaders: SecurityHeadersFilter) extends HttpFilters {
  val filters = Seq(gzip, csrf, securityHeaders)
}
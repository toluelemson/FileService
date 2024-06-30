import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class AppConfig {

    @Bean
    open fun objectMapper(): ObjectMapper {
        return ObjectMapper()
    }
}

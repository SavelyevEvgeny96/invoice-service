package ru.sogaz.site.orderingService.dao.impl

import org.springframework.stereotype.Service
import ru.sogaz.site.orderingService.dao.ClientSystemDao
import ru.sogaz.site.orderingService.entity.ClientSystemEntity
import ru.sogaz.site.orderingService.repository.ClientSystemRepository

@Service
class ClientSystemDaoImpl(
    private val clientSystemRepository: ClientSystemRepository,
) : ClientSystemDao {
    override fun checkingRefundAccess(codes: Collection<String>): List<ClientSystemEntity> =
        clientSystemRepository.findAllByExternalSystemCodeInAndPermissionReturnTrue(codes)
}

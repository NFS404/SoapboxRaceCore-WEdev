/*
 * This file is part of the Soapbox Race World core source code.
 * If you use any of this code for third-party purposes, please provide attribution.
 * Copyright (c) 2020.
 */

package com.soapboxrace.core.engine;

public enum EngineExceptionCode {
    ArgumentNullOrEmpty(4),
    AuthenticationTokenMissing(-800),
    BannedEntitlements(-1613),
    CantDeleteLastOwnedCar(-209),
    CarDataInvalid(-205),
    CarIsntCustomCar(-203),
    CarIsntPresetCar(-202),
    CarNotOwnedByDriver(-201),
    ChatConnectionError(-100),
    ConfigFilesAuthenticationException(-519),
    CountryBanned(-726),
    CountryInvalid(-725),
    CountryMissing(-723),
    CustomCarDoesntExist(-200),
    DateOfBirthInvalid(-721),
    DateOfBirthMissing(-720),
    DateOfBirthTooYoung(-722),
    DisplayNameDuplicate(-762),
    DisplayNameMissing(-761),
    DisplayNameNotAllowed(-765),
    DisplayNameSuggestionFailed(-766),
    DisplayNameTooLong(-763),
    DisplayNameTooShort(-764),
    EmailDuplicate(-713),
    EmailInvalid(-710),
    EmailInvalidDomain(-711),
    EmailMissing(-712),
    EntitlementConcurrencyFailure(-1909),
    EntitlementGrantDateInvalid(-1906),
    EntitlementInvalidCount(-1910),
    EntitlementNoSuchGroup(-1908),
    EntitlementProductIdMissing(-1900),
    EntitlementProductIdTooLong(-1902),
    EntitlementProductIdTooShort(-1901),
    EntitlementTagMissing(-1903),
    EntitlementTagTooLong(-1905),
    EntitlementTagTooShort(-1904),
    EntitlementTerminationDateInvalid(-1907),
    EventNotFound(-1550),
    FailedDeleteSession(-508),
    FailedPersonaIdComparison(-505),
    FailedPlayGameEntitlementCheck(-516),
    FailedPresenceCheck(-507),
    FailedReadSession(-510),
    FailedSessionInsertion(-506),
    FailedSessionSecurityPolicy(1503),
    FailedUpdateSession(-509),
    FailedUserIdComparison(-504),
    FriendAlreadyAdded(-901),
    FriendDoesNotExist(-902),
    FriendIsSelf(-900),
    FriendsListExceededMaximumCount(-903),
    GameDoesNotExist(-402),
    GameIsPrivate(-398),
    GameLocked(-399),
    GameServerRegionDoesntExist(101),
    GlobalOptInInvalid(-738),
    GlobalOptInMissing(-737),
    InsufficientFunds(-302),
    InsufficientInventory(-352),
    InvalidBasket(-356),
    InvalidCatalog(-355),
    InvalidCreditAmount(-306),
    InvalidCurrencyType(300),
    InvalidDebitAmount(-305),
    InvalidEntrantEventSession(-600),
    InvalidFinishReason(-603),
    InvalidFractionCompletedInResult(-617),
    InvalidGenderErrorCode(-758),
    InvalidHeaderValues(-502),
    InvalidOperation(8),
    InvalidPaintGroupForPaintSlot(-208),
    InvalidPlacingInResult(-616),
    InvalidPresence(-513),
    InvalidRaceTimeInResult(-614),
    InvalidRequestorIdHeader(-700),
    InvalidRewardModeForTelemetry(-6667),
    InvalidSpeedInResult(-615),
    InvalidUserId(-512),
    InvalidWalletType(-307),
    InventoryItemDoesntExist(-350),
    InviteAutoDeclined(-404),
    ItemIsForAnotherTier(-353),
    LanguageInvalid(-728),
    LanguageMissing(-727),
    LastAuthenticatedDateInvalid(-769),
    LastAuthenticatedOnDateInvalid(-744),
    LeftIsGreaterThanRight(9),
    LeftIsLessThanRight(10),
    LiveUpdateAuthenticationFailed(-100000),
    LiveUpdateInvalidCatalogVersion(-100001),
    LoginFailureLimitReached(-520),
    LuckyDrawCannotDeterminePersonaLevel(-1525),
    LuckyDrawContextNotFoundOrEmpty(-1526),
    LuckyDrawCouldNotDrawProduct(-1524),
    LuckyDrawInvalidDraw(-1521),
    LuckyDrawNoLootTablesNotPopulated(-1523),
    LuckyDrawNoMoreDraws(-1522),
    LuckyDrawNoTableDefinedForRace(-1520),
    MaxStackOrRentalLimitErrorCode(-417),
    MaximumNumberOfPersonasForUserReached(-788),
    MaximumNumberOfPersonasInNamespaceReached(-770),
    MaximumUsersLoggedInHardCapReached(-521),
    MaximumUsersLoggedInSoftCapReached(-522),
    MaximumUsersLoggedInUnspecified(-523),
    MissingMethodInputData(2),
    MissingRequiredEntitlements(-1612),
    MoreThanOneRemotePersonaFound(-2000),
    MoreThanOneRemoteUserFound(-775),
    MultiplePaintsInSameSlot(-207),
    NoPricepointErrorCode(-1917),
    NoProductErrorCode(-1911),
    NoSuchEntitlementExists(-3000),
    NoSuchInfoValueInSessionCurrentTable(-514),
    NoSuchSessionInSessionStore(-511),
    NoWalletErrorCode(-1683),
    NotEnoughSpace(-401),
    NotFound(-10404),
    NotInGame(-400),
    NotInQueue(-1801),
    NotPermanentSessionKey(-517),
    NotPrivateGame(-403),
    NullValue(-1002),
    PasswordEmailCombinationDuplicate(-718),
    PasswordIncorrect(-747),
    PasswordMissing(-714),
    PasswordNoSpacesAllowed(-717),
    PasswordTooLong(-716),
    PasswordTooShort(-715),
    PersonaCarIsNull(-782),
    PersonaMottoIsTooLong(-783),
    PersonaNotFound(-771),
    PlayerNotRanked(-1670),
    PowerUpItemInfoDoesntExist(-206),
    PresetCarDoesntExist(-210),
    RecursiveCatalog(-354),
    RegistrationSourceTooLong(-749),
    RemoteAccountManagementTimeout(-11000),
    RemoteNamespaceDoesNotExist(-760),
    RemotePersonaDoesNotBelongToUser(-773),
    RemotePersonaIdInvalid(-1646),
    RemoteUserDoesNotExist(-746),
    RemoteUserIsBanned(-748),
    RemoteUserIsGameBanned(-750),
    RemoteUserIsTempGameBanned(-751),
    RequiredHeadersNotFound(-501),
    SecurityKickedArbitration(-200000),
    SecurityKickedInvalidPowerup(-200005),
    SecurityKickedRaceTime(-200001),
    SecurityKickedRaceTimeRatio(-200002),
    SecurityKickedStatisticalRaceTime(-200003),
    SecurityKickedStatisticalTopSpeed(-200004),
    ServerConfigDoesntExist(102),
    ServerConfigNotFound(-2500),
    SessionKeyRequiredButNotFound(-6666),
    SessionRequestThresholdReached(-524),
    SocialFriendRequestNotResolvable(-10000),
    StatusIllegalValue(-733),
    StatusInvalid(-732),
    StatusMissing(-731),
    StatusReasonCodeInvalid(-768),
    SuggestiveSalesCannotDeterminePersonaLevel(-1602),
    SuggestiveSalesInvalidConfiguration(-1601),
    SuggestiveSalesInvalidContext(-1600),
    TargetFriendsListExceededMaximumCount(-904),
    ThirdPartyOptInInvalid(-740),
    ThirdPartyOptInMissing(-739),
    TooManySuggestionsRequested(-787),
    TosVersionMissing(-729),
    TosVersionTooLong(-730),
    TransactionAlreadyActive(-880),
    UnableToAuthenticateUserByAuthKey(-777),
    Undefined(0),
    Unknown(1),
    UnknownRemoteAccountManagementError(6),
    UnspecifiedError(-500),
    UnsupportedCurrency(-304),
    UserHasNoEntitlements(-1730),
    UserNotFound(-774),
    UsernameIsNotAllowed(-708),
    ValuesAreEqual(7),
    ValuesAreNotEqual(3),
    VirtualItemTypeDoesntExist(-351),
    VoipRemoteCallTimeout(-11001),
    VoipUnknownError(-11002),
    WalletAlreadyExists(-303),
    WalletBalanceIsNotEnough(-1684),
    WalletDoesntExist(-301),
    WalletNotUserWallet(-308),
    WebTokenCheckFailed(-518),
    WrongClientVersion(-799),
    XmlDeserializationError(5);

    private final int errorCode;

    EngineExceptionCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
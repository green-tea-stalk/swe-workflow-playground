package com.example.bulletinboard.exception;

/**
 * 掲示板ドメインにおける基底実行時例外クラス。
 * アプリケーション内で発生する業務エラーおよびインフラ層エラーの親クラスとして機能する。
 */
public abstract class BulletinBoardException extends RuntimeException {

    /**
     * エラーメッセージを指定して例外を生成する。
     *
     * @param message エラー詳細メッセージ
     */
    protected BulletinBoardException(String message) {
        super(message);
    }

    /**
     * エラーメッセージおよび原因となった元例外を指定して例外を生成する。
     *
     * @param message エラー詳細メッセージ
     * @param cause 原因例外
     */
    protected BulletinBoardException(String message, Throwable cause) {
        super(message, cause);
    }
}
